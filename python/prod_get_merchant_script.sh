#!/bin/sh

if [ ! -d .venv ]; then
    python3 -m venv .venv
fi

if [ -z "$VIRTUAL_ENV" ]; then
  echo "Python virtual environment is not active"
  source .venv/bin/activate
  pip install -r python/requirements.txt
  PIP_STATUS=$?
  if [ $PIP_STATUS -ne 0 ]; then
    echo "pip install failed. Exiting."
    exit $PIP_STATUS
  fi
  python3 python/prod_get_merchant.py "$1" "$2" "$3"
else
  echo "Python virtual environment is active"
  python3 python/prod_get_merchant.py "$1" "$2" "$3"
fi