"""Quick check: python test_server.py http://127.0.0.1:8765 some.wav [am|en]"""
import json
import sys
import urllib.request

base, path = sys.argv[1], sys.argv[2]
lang = sys.argv[3] if len(sys.argv) > 3 else "am"
print(urllib.request.urlopen(base + "/health", timeout=5).read().decode())
req = urllib.request.Request(f"{base}/transcribe?lang={lang}", data=open(path, "rb").read(), headers={"Content-Type": "audio/wav"})
print(json.loads(urllib.request.urlopen(req, timeout=60).read()))
