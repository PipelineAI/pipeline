#!/bin/bash

echo "Activating 'model_environment_python3'..."
source activate model_environment_python3
echo "...Done!"

#export PIO_MODEL_FILENAME=$(ls *.pkl | sed -n 1p)

echo "Testing model..."
[ -s ./test_model.py ] && python test_model.py \
                                "./test_inputs.txt" \
                                "./test_outputs.txt"

echo "...Done!"
