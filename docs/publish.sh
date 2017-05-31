#!/bin/bash

pip uninstall -y mkdocs-material
pip install ./mkdocs-material/

rm -rf generated/

mkdocs gh-deploy
