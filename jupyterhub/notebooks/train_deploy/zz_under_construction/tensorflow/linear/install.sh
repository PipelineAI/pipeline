#!/bin/bash

echo "Installing model and dependencies..."

source activate root

# Create a new, empty `model_environment` environment.
conda create --yes -n model_environment_python3 python=3.5

# Activate model_environment
source activate model_environment_python3

# Check for (non-empty) conda_requirements.txt and `conda install` from it.
[ -s ./requirements_conda.txt ] && conda install --yes --file ./requirements_conda.txt

# Check for (non-empty) requirements.txt and `pip install` it.
[ -s ./requirements.txt ] && pip install -r ./requirements.txt

echo "...Done!"
