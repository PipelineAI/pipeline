#!/bin/bash

# TODO:  Clean up this logic
# TODO:  The pipeline_train.py file is expected to place the saved model in the right directory
# TODO:  If using TensorFlow Serving, ensure that $PIPELINE_MODEL_PATH/versions directory exists after training

#if [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]
if [ -f "$PIPELINE_MODEL_PATH/pipeline_train.sh" ]; then
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE"
     echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME"
     echo "PIPELINE_MODEL_CHIP=$PIPELINE_MODEL_CHIP"
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo ""
     echo "Starting TensorFlow-based Model Training..."
     echo ""
     source activate $PIPELINE_CONDA_ENV_NAME
     cd $PIPELINE_MODEL_PATH
     python pipeline_train.py
     cd /root
     echo ""
     echo "...Training Complete!"
     echo ""
fi

#if [[ $PIPELINE_MODEL_TYPE == "python" ]] &
if [ -f "$PIPELINE_MODEL_PATH/pipeline_train.py" ]; then
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE"
     echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME"
     echo "PIPELINE_MODEL_CHIP=$PIPELINE_MODEL_CHIP"
     echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH"
     echo ""
     echo "Starting Python-based Model Training..."
     echo ""
     source activate $PIPELINE_CONDA_ENV_NAME
     cd $PIPELINE_MODEL_PATH
     python pipeline_train.py
     cd /root
     echo ""
     echo "...Training Complete!"
     echo ""
fi
