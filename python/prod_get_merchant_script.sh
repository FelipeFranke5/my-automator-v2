#!/bin/sh

MAX_PROCESSES=3
WAIT_SECONDS=20

# Count Python processes
count_python_processes() {
  ps -ef | grep "python3" | grep "prod_get_merchant.py" | grep -v grep | wc -l
}

# If there are too many processes, wait
while [ "$(count_python_processes)" -ge "$MAX_PROCESSES" ]; do
  echo "Limit for $MAX_PROCESSES processes reached. Awaiting $WAIT_SECONDS seconds..."
  sleep $WAIT_SECONDS
done

if [ ! -d .venv ]; then
    python3 -m venv .venv
fi

if [[ -z "$VIRTUAL_ENV" ]]; then
  echo "Python virtual environment is not active"
  source .venv/bin/activate
  pip install -r python/requirements.txt && python3 python/prod_get_merchant.py "$1" "$2" "$3"
else
  echo "Python virtual environment is active"
  python3 python/prod_get_merchant.py "$1" "$2" "$3"
fi