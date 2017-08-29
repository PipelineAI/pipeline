#!/bin/bash

if [[ $PIPELINE_MODEL_TYPE == "python" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "keras" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "scikit" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then
       echo ""
       echo "Deleting '$PIPELINE_CONDA_ENV_NAME' Conda Environment'..."
       echo ""
       conda env remove -q --name $PIPELINE_CONDA_ENV_NAME
       echo ""
       echo "...Deleted!"
       echo ""
fi
