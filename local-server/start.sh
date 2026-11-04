#!/usr/bin/env bash
# Starts the optional local AI server (same Wi-Fi as the phone). See README.md.
set -euo pipefail
cd "$(dirname "$0")"
[ -d .venv ] || python3 -m venv .venv
. .venv/bin/activate
pip install -q -r requirements.txt
exec python server.py "$@"
