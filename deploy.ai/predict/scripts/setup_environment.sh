echo "" 
echo "Creating model_server Environment and Installing $PIO_MODEL_TYPE/$PIO_MODEL_NAME Dependencies..." 
echo ""

# Model
#[ -s $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements_conda.txt ] \
conda create --yes --name model_server --file $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements_conda.txt

[ -s $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements.txt ] \
  && pip install -r $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements.txt 
echo "" 
echo "...Model Dependencies Installed!" 
echo ""

echo ""
echo "Installing Model Server Dependencies..."
echo ""

# Model Server
[ -s $PIO_MODEL_SERVER_PATH/requirements/$PIO_MODEL_TYPE/requirements_conda_pio_model_server.txt ] \
  && conda install --no-update-dependencies -n model_server --yes --file $PIO_MODEL_SERVER_PATH/requirements/$PIO_MODEL_TYPE/requirements_conda_pio_model_server.txt

[ -s $PIO_MODEL_SERVER_PATH/requirements/$PIO_MODEL_TYPE/requirements_pio_model_server.txt ] \
  && pip install -r $PIO_MODEL_SERVER_PATH/requirements/$PIO_MODEL_TYPE/requirements_pio_model_server.txt
echo ""
echo "...Model Server Dependencies Installed!"
echo ""
