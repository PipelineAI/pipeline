# Args
#   $1: /path/to/source.ml
docker run --name=prediction-python -itd -e PIO_MODEL_NAMESPACE=default -e PIO_MODEL_NAME=scikit_balancescale -e PIO_MODEL_VERSION=v0 -v $1:/root/volumes/source.ml fluxcapacitor/prediction-python:master
