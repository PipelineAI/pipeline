#!/bin/bash

# The pipeline_train.py file is expected to place the saved model in the right directory
# If using TensorFlow Serving, ensure that $PIPELINE_MODEL_PATH/versions directory exists after training
if [ -f "$PIPELINE_MODEL_PATH/pipeline_train.py" ]; then
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE"
     echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME"
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo ""
     echo "Starting Model Training..."
     echo ""
     source activate $PIPELINE_MODEL_CONDA_ENV_NAME
     cd $PIPELINE_MODEL_PATH
     python pipeline_train.py
     cd /root
     echo ""
     echo "...Training Complete!"
     echo ""
fi
