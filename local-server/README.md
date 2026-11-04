# Local AI server (optional)

The app's AI runs **inside the APK**, on the phone, with no internet. This folder adds an
optional helper for when a computer is nearby: the phone sends each spoken sentence to the
computer over the local Wi-Fi, and uses the computer's transcription as one more opinion.
If the computer is off, slow (more than about 3.5 s) or unreachable, the phone carries on alone.

## Run

```bash
# once, from the repository root: puts the models in app/src/main/assets/models
./tools/fetch_models.sh
# then
./local-server/start.sh                 # Windows: local-server\start.bat
./local-server/start.sh --whisper small.en   # optional: faster-whisper for English (pip install faster-whisper)
./local-server/start.sh --omni-dir /path/to/a/larger/sherpa-onnx-omnilingual-ctc-model
```

It prints an address such as `http://192.168.1.20:8765`. In the app: **Home → Support** (bottom
bar) → enter the address → **Save & test**. Leave it empty to use the phone only.

The phone and the computer must be on the same Wi-Fi; allow port 8765 in the computer's firewall.
Nothing goes to the internet.

## API

- `GET /health` → `{"ok": true, "models": "..."}`
- `POST /transcribe?lang=am|en` with a 16 kHz mono 16-bit WAV → `{"text": "...", "ms": 120}`

Test: `python local-server/test_server.py http://127.0.0.1:8765 sample.wav am`
