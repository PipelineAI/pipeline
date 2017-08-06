FROM fluxcapacitor/predict-model-server:master

# !!!!!!!!!!!!!!!!!! REQUIREMENTS !!!!!!!!!!!!!!!!!!
#   * S3 does not support underscores "_" in bucket names
#   * Python does not support dashes "-" in package directory paths

# Docker Build Arguments used to dynamically set model type, name and version
ARG MODEL_S3_OBJECT_NAME
RUN \
  echo $MODEL_S3_OBJECT_NAME
ENV \
  MODEL_S3_OBJECT_NAME=$MODEL_S3_OBJECT_NAME

# Data Science Model artifacts
# 1st the Jenkinsfile downloads the Model artifacts from S3 using the Jenkins pipeline-aws-plugin
# https://github.com/jenkinsci/pipeline-aws-plugin
# 2nd COPY Model artifacts from Jenkins working directory into the container
COPY model/$MODEL_S3_OBJECT_NAME/ $PIPELINE_MODEL_PATH

# Install conda and pip packages required by the Data Science Model
RUN \
  echo "" \
  && echo "Creating and Activating '$PIPELINE_CONDA_ENV_NAME' Conda Environment with '$MODEL_S3_OBJECT_NAME' Model Dependencies with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." \
  && echo "" \
  && conda env create --name $PIPELINE_CONDA_ENV_NAME \
    --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml \
  && echo "" \
  && echo "...Created and Activated!" \
  && echo ""

# Install conda and pip packages required to run the Tornado REST API and Metrics Monitoring
RUN \
  echo "" \
  && echo "Installing Model Server Dependencies..." \
  && echo "" \
  && conda env update --name $PIPELINE_CONDA_ENV_NAME \
    --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml \
  && echo "" \
  && echo "...Model Server Dependencies Installed!" \
  && echo ""

# Activate Conda Environment
RUN \
  source activate $PIPELINE_CONDA_ENV_NAME

# Export a list of all conda linked packages installed in the conda environment to a file that can be reviewed via the REST API
RUN \
  conda list -n $PIPELINE_CONDA_ENV_NAME >$PIPELINE_MODEL_SERVER_PATH/static/conda-list-environment.txt

# Expose port 80 for the Tornado REST API and port 10254 for metrics monitoring
EXPOSE 80 10254

# Execute the "run" script which executes model training and starts the Tornado REST API server
# SUPERVISE restarts the Python Tornado REST API if the process stops
CMD ["supervise", "."]
