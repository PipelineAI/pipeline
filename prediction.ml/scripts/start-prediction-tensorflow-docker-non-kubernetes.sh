# Args
#   $1: /path/to/source.ml
docker run --name=prediction-tensorflow -itd -e PIO_MODEL_NAMESPACE=default -e PIO_MODEL_NAME=tensorflow_linear -e PIO_MODEL_VERSION=0 -v $1:/root/volumes/source.ml fluxcapacitor/prediction-tensorflow:master
