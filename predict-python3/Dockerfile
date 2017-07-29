FROM fluxcapacitor/predict-model-server:master

ARG MODEL_TYPE
RUN \
  echo $MODEL_TYPE
ENV \
  PIPELINE_MODEL_TYPE=$MODEL_TYPE

ARG MODEL_NAME
RUN \
  echo $MODEL_NAME
ENV \
  PIPELINE_MODEL_NAME=$MODEL_NAME

ARG MODEL_VERSION
RUN \
  echo $MODEL_VERSION
ENV \
  PIPELINE_MODEL_VERSION=$MODEL_VERSION

COPY model/model-store/$MODEL_TYPE/$MODEL_NAME/$MODEL_VERSION/ $PIPELINE_MODEL_PATH

RUN \
  echo "" \
  && echo "Creating and Activating '$PIPELINE_CONDA_ENV_NAME' Conda Environment with '$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME' Model Dependencies with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." \
  && echo "" \
  && conda env create --name $PIPELINE_CONDA_ENV_NAME \
    --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml \
  && echo "" \
  && echo "...Created and Activated!" \
  && echo ""

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

RUN \
  conda list -n $PIPELINE_CONDA_ENV_NAME >/var/www/html/conda-list-$PIPELINE_CONDA_ENV_NAME.txt

EXPOSE 80 6969 9876 10254

CMD ["supervise", "."]
