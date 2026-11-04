@echo off
rem Starts the optional local AI server (same Wi-Fi as the phone). See README.md.
cd /d %~dp0
if not exist .venv python -m venv .venv
call .venv\Scripts\activate
pip install -q -r requirements.txt
python server.py %*
