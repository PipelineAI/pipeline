#!/bin/bash

if [ -e "$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml" ]; then
  echo ""
  echo "Creating Conda Environment '$PIPELINE_MODEL_CONDA_ENV_NAME' with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..."
  echo ""
  conda env update --name $PIPELINE_MODEL_CONDA_ENV_NAME --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml
  echo ""
  echo "...Conda Environment Updated!"
  echo ""
  echo ""
  echo "Setting up Model Server into Conda Environment '$PIPELINE_MODEL_CONDA_ENV_NAME' with '$PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml'..."
  echo ""
  conda env update --name $PIPELINE_MODEL_CONDA_ENV_NAME --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml
  echo ""
  echo "...Model Server Installed!"
  echo ""

  echo "source activate $PIPELINE_MODEL_CONDA_ENV_NAME" >> ~/.bashrc

  if [ -e "$PIPELINE_MODEL_PATH/pipeline_install.sh" ]; then
    CONDA_ENV_ROOT_PATH=$(conda info --root)
    echo "Installing '$PIPELINE_MODEL_PATH/pipeline_install.sh' into Conda Environment '$PIPELINE_MODEL_CONDA_ENV_NAME'..."
    conda info --root
    conda env list
    echo ""
    source activate $PIPELINE_MODEL_CONDA_ENV_NAME
    echo "Running '$PIPELINE_MODEL_PATH/pipeline_install.sh'..."
    chmod a+x $PIPELINE_MODEL_PATH/pipeline_install.sh
    $PIPELINE_MODEL_PATH/pipeline_install.sh
    export
    echo ""
    echo "...Successfully Installed!"
    echo ""
  fi
fi
