# Args
#   $1: /path/to/source.ml
docker run --name=prediction-tensorflow -itd -p 81:9876 -p 8081:8080 -e PIO_MODEL_TYPE=tensorflow -e PIO_MODEL_NAMESPACE=default -e PIO_MODEL_NAME=tensorflow_linear -e PIO_MODEL_VERSION=0 -v $1:/root/volumes/source.ml fluxcapacitor/prediction-tensorflow:master
