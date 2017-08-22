#!/bin/bash

echo "Activating 'model_environment_python3'..."
source activate model_environment_python3
echo "...Done!"

python model.py model.pkl
