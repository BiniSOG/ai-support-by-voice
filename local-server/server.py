#!/usr/bin/env python3
"""
Optional AI helper that runs on the customer's own computer (no internet needed).

The phone app always works on its own with the models inside the APK. When this server runs
on a computer on the same Wi-Fi, the app also sends each spoken sentence here and uses the
computer's transcription as one more opinion (whichever answers the current question wins).
If the computer is off or slow, the phone simply carries on alone.

  POST /transcribe?lang=am|en   body: 16 kHz mono 16-bit WAV   ->  {"text": "...", "ms": 123}
  GET  /health                                                 ->  {"ok": true, "models": "..."}

Models (all local files):
  * Amharic (and English fallback): any sherpa-onnx Omnilingual ASR CTC model directory
    (model.onnx or model.int8.onnx + tokens.txt). Default: the one bundled in the app
    (../app/src/main/assets/models/asr-am after tools/fetch_models.sh). A computer can run
    a larger one, e.g. --omni-dir path/to/sherpa-onnx-omnilingual-asr-...-1B-ctc...
  * English: faster-whisper (if installed), e.g. --whisper small.en / medium.en / large-v3.
"""
import argparse
import io
import json
import os
import socket
import sys
import threading
import time
import wave
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_OMNI = os.path.join(HERE, "..", "app", "src", "main", "assets", "models", "asr-am")


def load_omnilingual(model_dir, threads):
    import sherpa_onnx

    model = next((os.path.join(model_dir, n) for n in ("model.onnx", "model.int8.onnx") if os.path.exists(os.path.join(model_dir, n))), None)
    tokens = os.path.join(model_dir, "tokens.txt")
    if model is None or not os.path.exists(tokens):
        sys.exit(f"No Omnilingual model in {model_dir} (need model.onnx or model.int8.onnx and tokens.txt).\n"
                 "Run tools/fetch_models.sh first, or pass --omni-dir.")
    rec = sherpa_onnx.OfflineRecognizer.from_omnilingual_asr_ctc(model=model, tokens=tokens, num_threads=threads)
    return rec, os.path.basename(os.path.normpath(model_dir))


def load_whisper(name):
    if not name:
        return None
    try:
        from faster_whisper import WhisperModel
    except ImportError:
        print("faster-whisper not installed; English uses Omnilingual. (pip install faster-whisper)")
        return None
    return WhisperModel(name, device="auto", compute_type="int8")


def read_wav(data):
    with wave.open(io.BytesIO(data)) as w:
        if w.getsampwidth() != 2 or w.getnchannels() != 1:
            raise ValueError("expected 16-bit mono WAV")
        rate = w.getframerate()
        x = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float32) / 32768.0
    if rate != 16000:
        n = int(len(x) * 16000 / rate)
        x = np.interp(np.linspace(0, len(x) - 1, n), np.arange(len(x)), x).astype(np.float32)
    return x


def normalize(x):
    rms = float(np.sqrt(np.mean(x * x))) if len(x) else 0.0
    peak = float(np.max(np.abs(x))) if len(x) else 0.0
    if rms < 1e-5 or peak < 1e-4:
        return x
    return x * min(0.08 / rms, 0.95 / peak, 8.0)


class Engine:
    def __init__(self, args):
        self.omni, self.omni_name = load_omnilingual(args.omni_dir, args.threads)
        self.whisper = load_whisper(args.whisper)
        self.lock = threading.Lock()
        self.models = self.omni_name + (f" + whisper {args.whisper}" if self.whisper else "")

    def transcribe(self, x, lang):
        x = normalize(x)
        with self.lock:
            if lang == "en" and self.whisper is not None:
                segs, _ = self.whisper.transcribe(x, language="en", beam_size=5, vad_filter=False)
                return " ".join(s.text.strip() for s in segs).strip()
            s = self.omni.create_stream()
            s.accept_waveform(16000, x)
            self.omni.decode_stream(s)
            return s.result.text.strip()


def make_handler(engine):
    class Handler(BaseHTTPRequestHandler):
        def _json(self, code, obj):
            body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
            self.send_response(code)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def do_GET(self):
            if urlparse(self.path).path == "/health":
                self._json(200, {"ok": True, "models": engine.models})
            else:
                self._json(404, {"error": "not found"})

        def do_POST(self):
            u = urlparse(self.path)
            if u.path != "/transcribe":
                return self._json(404, {"error": "not found"})
            lang = parse_qs(u.query).get("lang", ["am"])[0]
            n = int(self.headers.get("Content-Length", "0"))
            if n <= 44 or n > 20 * 1024 * 1024:
                return self._json(400, {"error": "bad body"})
            try:
                t0 = time.time()
                text = engine.transcribe(read_wav(self.rfile.read(n)), lang)
                ms = int((time.time() - t0) * 1000)
                print(f"[{lang}] {ms} ms: {text}")
                self._json(200, {"text": text, "ms": ms})
            except Exception as e:  # noqa: BLE001 - report to the phone, keep serving
                self._json(500, {"error": str(e)})

        def log_message(self, *a):
            pass

    return Handler


def lan_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("10.255.255.255", 1))  # no packet is sent
        return s.getsockname()[0]
    except OSError:
        return "127.0.0.1"
    finally:
        s.close()


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--host", default="0.0.0.0")
    p.add_argument("--port", type=int, default=8765)
    p.add_argument("--omni-dir", default=DEFAULT_OMNI)
    p.add_argument("--whisper", default="", help="faster-whisper model for English, e.g. small.en")
    p.add_argument("--threads", type=int, default=max(2, (os.cpu_count() or 4) // 2))
    args = p.parse_args()
    engine = Engine(args)
    srv = ThreadingHTTPServer((args.host, args.port), make_handler(engine))
    print(f"Models: {engine.models}")
    print(f"In the app: Home > Support > enter  http://{lan_ip()}:{args.port}")
    srv.serve_forever()


if __name__ == "__main__":
    main()
