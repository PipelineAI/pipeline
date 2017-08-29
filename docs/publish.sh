#!/bin/bash

pip uninstall -y mkdocs-material
pip uninstall -y mkdocs-bootstrap
pip install ./mkdocs-material/
pip install mkdocs-bootstrap
pip install markdown-include
pip install pygments

rm -rf generated/

mkdocs gh-deploy
