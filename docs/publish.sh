#!/bin/bash

pip uninstall -y mkdocs-material
pip install ./src/mkdocs-material

mkdocs gh-deploy
