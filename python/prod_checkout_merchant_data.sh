#!/bin/sh

source .venv/bin/activate
python3 python/prod_checkout_merchant_data.py "$1" "$2" "$3"