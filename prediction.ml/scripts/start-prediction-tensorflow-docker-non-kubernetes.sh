# Args
#   $1: /path/to/source.ml
docker run --name=prediction-tensorflow -itd -v $1:/root/volumes/source.ml fluxcapacitor/prediction-tensorflow:master
