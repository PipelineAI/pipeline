echo "PIO_MODEL_STORE_HOME=$PIO_MODEL_STORE_HOME"
echo "PIO_MODEL_TYPE=$PIO_MODEL_TYPE"
echo "PIO_MODEL_NAME=$PIO_MODEL_NAME"

echo ""
echo "Installing Model Dependencies..."
echo ""
# Conda
[ -s $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements_conda.txt ] \
  && conda install --yes --file $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements_conda.txt
# Pip
[ -s $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements.txt ] \
  && pip install -r $PIO_MODEL_STORE_HOME/$PIO_MODEL_TYPE/$PIO_MODEL_NAME/requirements.txt
echo ""
echo "...Model Dependencies Installed!"
echo ""
