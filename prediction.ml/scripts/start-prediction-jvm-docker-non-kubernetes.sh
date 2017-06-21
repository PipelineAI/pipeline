# Args

docker run --name=prediction-jvm -itd -p 82:9040 -p 8082:8080 -v $1:/root/volumes/source.ml fluxcapacitor/prediction-jvm:master
