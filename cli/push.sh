# removed sdist (source distribution)
# TODO:  move to twine
#!/bin/bash
rm -rf build/
rm -rf dist/
rm -rf cli_pipeline.egg-info
rm -rf cli_pipeline/__pycache__

mypy cli_pipeline/__init__.py
mypy cli_pipeline/cli_pipeline.py

python setup.py bdist_wheel --universal upload

