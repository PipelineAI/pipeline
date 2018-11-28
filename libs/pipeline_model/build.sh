#!/bin/bash

mypy pipeline_model/__init__.py

pip uninstall . && pip install -U --no-cache --ignore-installed -e .
