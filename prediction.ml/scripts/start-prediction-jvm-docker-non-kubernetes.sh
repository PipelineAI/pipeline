# Args
#   $1: http port
#   $2: /path/to/source.ml
docker run --name=prediction-jvm -itd -p $1:9040 -v $2:/root/volumes/source.ml fluxcapacitor/prediction-jvm:master
