#!/bin/bash

if [[ $PIPELINE_MODEL_TYPE == "python" ]] || \
   [[ $PIPELINE_MODEL_TYPE == "keras" ]] || \
   [[ $PIPELINE_MODEL_TYPE == "scikit" ]] || \
   [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then 
  echo "" 
  echo "Creating and Activating Conda Environment '$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME' with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." 
  echo "" 
  conda env create --name $PIPELINE_CONDA_ENV_NAME --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml 
  echo "" 
  echo "...Conda Environment Created and Activated!" 
  echo ""
  echo ""
  echo "Setting up Model Server into Conda Environment '$PIPELINE_CONDA_ENV_NAME'..."
  echo ""
  conda env update --name $PIPELINE_CONDA_ENV_NAME --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml
  echo ""
  echo "source activate $PIPELINE_CONDA_ENV_NAME" >> ~/.bashrc
  echo "...Model Server Installed!"
  echo ""
fi

if [ -e "$PIPELINE_MODEL_PATH/pipeline_install.sh" ]; then 
  CONDA_ENV_ROOT_PATH=$(conda info --root) 
  echo "Installing '$PIPELINE_MODEL_PATH/pipeline_install.sh' into Conda Environment '$PIPELINE_CONDA_ENV_NAME'..." 
  conda info --root 
  conda env list 
  echo "" 
  source activate $PIPELINE_CONDA_ENV_NAME 
  echo "Running '$PIPELINE_MODEL_PATH/pipeline_install.sh':\n" 
  chmod a+x $PIPELINE_MODEL_PATH/pipeline_install.sh 
  $PIPELINE_MODEL_PATH/pipeline_install.sh 
  export 
  echo "...Successfully Installed!" 
  echo "" 
fi
