#!/bin/sh

source .venv/bin/activate
python3 python/prod_get_merchant.py "$1" "$2" "$3"