echo ""
echo "Setting up TensorBoard into Conda Environment '$PIPELINE_TENSORBOARD_CONDA_ENV_NAME'"
conda create --name $PIPELINE_TENSORBOARD_CONDA_ENV_NAME
source activate $PIPELINE_TENSORBOARD_CONDA_ENV_NAME
pip install tensorflow==$PIPELINE_TENSORBOARD_TENSORFLOW_VERSION
echo ""
echo "...TensorBoard Installed!"
echo ""
