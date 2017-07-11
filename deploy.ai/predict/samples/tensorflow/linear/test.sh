#!/bin/bash 

# Check for (non-empty) conda_requirements.txt and `conda install` from it.
[ -s ./requirements_conda.txt ] && conda install --yes --file ./requirements_conda.txt

# Check for (non-empty) requirements.txt and `pip install` it.
[ -s ./requirements.txt ] && pip install -r ./requirements.txt

python3 ./pipeline.py 
