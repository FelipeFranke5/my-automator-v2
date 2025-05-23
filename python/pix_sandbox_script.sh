#!/bin/sh

if [ ! -d .venv ]; then
    python3 -m venv .venv
fi

if [[ -z "$VIRTUAL_ENV" ]]; then
  echo "Python virtual environment is not active"
  source .venv/bin/activate
  pip install -r python/requirements.txt && python3 python/sandbox_pix2.py "$1" "$2" "$3"
else
  echo "Python virtual environment is active"
  python3 python/sandbox_pix2.py "$1" "$2" "$3"
fi