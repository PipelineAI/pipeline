#!/bin/bash

pip uninstall -y mkdocs-material
pip install ./mkdocs-material/
pip install markdown-include
pip install pygments

rm -rf generated/

mkdocs gh-deploy
