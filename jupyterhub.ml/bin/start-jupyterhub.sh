# Args

docker run --name=jupyterhub --net=host -itd -p 9754:8754 -p 9755:8755 -p 9006:6006 -v $1:/root/volumes/source.ml fluxcapacitor/jupyterhub:master
