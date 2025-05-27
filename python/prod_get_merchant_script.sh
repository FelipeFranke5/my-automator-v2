#!/bin/sh

LOCKFILE="/tmp/pip_install.lock"
TIMEOUT=300  # 5 minutes in seconds
WAIT_INTERVAL=5

if [ ! -d .venv ]; then
    python3 -m venv .venv
fi

if [ -z "$VIRTUAL_ENV" ]; then
  echo "Python virtual environment is not active"
  source .venv/bin/activate
  # Locking mechanism for pip install
  START_TIME=$(date +%s)
  while [ -f "$LOCKFILE" ]; do
    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))
    if [ $ELAPSED_TIME -ge $TIMEOUT ]; then
      echo "Timeout waiting for pip install lock. Exiting."
      exit 1
    fi
    echo "Another pip install is running. Waiting... ($SECONDS/$TIMEOUT seconds)"
    sleep $WAIT_INTERVAL
  done
  touch "$LOCKFILE"
  trap 'rm -f "$LOCKFILE"' EXIT
  pip install -r python/requirements.txt
  PIP_STATUS=$?
  rm -f "$LOCKFILE"
  if [ $PIP_STATUS -ne 0 ]; then
    echo "pip install failed. Exiting."
    exit $PIP_STATUS
  fi
  python3 python/prod_get_merchant.py "$1" "$2" "$3"
else
  echo "Python virtual environment is active"
  python3 python/prod_get_merchant.py "$1" "$2" "$3"
fi