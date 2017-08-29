#!/bin/bash

if [[ $PIPELINE_MODEL_TYPE == "python" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "keras" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "scikit" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then 
       echo "" 
       echo "Creating and Activating '$PIPELINE_CONDA_ENV_NAME' Conda Environment with '$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME' Model Dependencies with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." 
       echo "" 
       conda env create --name $PIPELINE_CONDA_ENV_NAME --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml 
       echo "" 
       echo "...Created and Activated!" 
       echo "" 
       echo "" 
       echo "Installing Model Server Dependencies..." 
       echo "" 
       conda env update --name $PIPELINE_CONDA_ENV_NAME --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml 
       echo "" 
       echo "source activate $PIPELINE_CONDA_ENV_NAME" >> ~/.bashrc 
       echo "...Model Server Dependencies Installed!" 
       echo "" 
fi

# TODO:  Include this in the above if statement

if [ -e "$PIPELINE_MODEL_PATH/pipeline_install.sh" ]; then 
     CONDA_ENV_ROOT_PATH=$(conda info --root) 
     echo "Setting Up Conda Environment with '$PIPELINE_MODEL_PATH/pipeline_install.sh'..." 
     conda info --root 
     conda env list 
     echo "" 
     source activate $PIPELINE_CONDA_ENV_NAME 
     echo "Running '$PIPELINE_MODEL_PATH/pipeline_install.sh':\n" 
     chmod a+x $PIPELINE_MODEL_PATH/pipeline_install.sh 
     $PIPELINE_MODEL_PATH/pipeline_install.sh 
     export 
     echo "...Conda Environment Successfully Setup at '$PIPELINE_MODEL_PATH/pipeline_install.sh'!" 
     echo "" 
fi

# TODO:  Clean up this logic
# TODO:  The pipeline_train.py file is expected to place the saved model in the right directory
# TODO:  If using TensorFlow Serving, ensure that $PIPELINE_MODEL_PATH/versions directory exists after training

if [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]] && [ -f "$PIPELINE_MODEL_PATH/pipeline_train.sh" ]; then 
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

if [[ $PIPELINE_MODEL_TYPE == "python" ]] && [ -f "$PIPELINE_MODEL_PATH/pipeline_train.py" ]; then 
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
