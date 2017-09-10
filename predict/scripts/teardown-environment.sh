#!/bin/bash

#if [[ $PIPELINE_MODEL_TYPE == "python" ]] || \
#     [[ $PIPELINE_MODEL_TYPE == "keras" ]] || \
#     [[ $PIPELINE_MODEL_TYPE == "scikit" ]] || \
#     [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then
       echo ""
       echo "Deleting '$PIPELINE_CONDA_ENV_NAME' Conda Environment'..."
       echo ""
       source activate root
       conda env remove --yes --name $PIPELINE_CONDA_ENV_NAME
       echo ""
       echo "...Deleted!"
       echo ""
#fi
