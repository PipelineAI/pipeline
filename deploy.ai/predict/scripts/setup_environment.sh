echo ""
echo "Creating and Activating '$PIO_CONDA_ENV_NAME' Conda Environment..."
echo ""
cat $PIO_MODEL_PATH/pio_environment_conda.yml \

conda env create --name $PIO_CONDA_ENV_NAME \
    --file $PIO_MODEL_PATH/pio_environment_conda.yml 

source activate $PIO_CONDA_ENV_NAME

echo ""
echo "...Conda Environment Created and Activated!"
echo ""

echo "" 
echo "Installing '$PIO_MODEL_TYPE/$PIO_MODEL_NAME' Model Dependencies..." 
echo ""
cat $PIO_MODEL_PATH/pio_requirements_conda.txt
conda install --yes --file $PIO_MODEL_PATH/pio_requirements_conda.txt

cat $PIO_MODEL_PATH/pio_requirements.txt
pip install -r $PIO_MODEL_PATH/pio_requirements.txt 
echo "" 
echo "...Model Dependencies Installed!" 
echo ""

echo ""
echo "Installing '$PIO_MODEL_TYPE' Server Dependencies..."
echo ""
if [[ $PIO_MODEL_TYPE = "tensorflow" ]]; then  
  cat $PIO_MODEL_SERVER_PATH/requirements/tensorflow/pio_model_server_requirements_conda.txt
  conda install --yes \
    --file $PIO_MODEL_SERVER_PATH/requirements/tensorflow/pio_model_server_requirements_conda.txt

  cat $PIO_MODEL_SERVER_PATH/requirements/tensorflow/pio_model_server_requirements.txt
  pip install \
    -r $PIO_MODEL_SERVER_PATH/requirements/tensorflow/pio_model_server_requirements.txt
else
  cat $PIO_MODEL_SERVER_PATH/requirements/python3/pio_model_server_requirements_conda.txt
  conda install --yes \
    --file $PIO_MODEL_SERVER_PATH/requirements/python3/pio_model_server_requirements_conda.txt

  cat $PIO_MODEL_SERVER_PATH/requirements/python3/pio_model_server_requirements.txt
  pip install \
    -r $PIO_MODEL_SERVER_PATH/requirements/python3/pio_model_server_requirements.txt
fi;
echo ""
echo "...Model Server Dependencies Installed!"
echo ""
