#!/bin/bash
# This script will install the docker binaries for working on OSX as well
# as creating a docker machine and importing the tar docker image
# Author Benjamin Rizkowsky benoahriz@gmail.com 

export USB=USB20FD
echo "version of docker-machine should be at least version 0.3.1-rc1 (993f2db)"
# curl -L "https://github.com/docker/machine/releases/download/v0.3.1-rc1/docker-machine_darwin-amd64" > /usr/local/bin/docker-machine

cat osx/docker-machine_darwin-amd64 > /usr/local/bin/docker-machine

echo "making docker-machine executable"
chmod +x /usr/local/bin/docker-machine

echo "check the version of docker-machine"
docker-machine -v || exit 0

echo "install the latest docker client binary for osx Docker version 1.7.1, build 786b29d"
# curl -L "https://get.docker.com/builds/Darwin/x86_64/docker-latest" > /usr/local/bin/docker
# MD5(docker-latest)= 680d6b89127c82dc70ed5807ce8ff019
cat osx/docker-latest > /usr/local/bin/docker
openssl md5  /usr/local/bin/docker

echo "making docker executable"
chmod +x /usr/local/bin/docker
docker -v || exit 0

echo "install the latest docker-compose binary"
# curl -L "https://github.com/docker/compose/releases/download/1.3.3/docker-compose-$(uname -s)-$(uname -m)" > /usr/local/bin/docker-compose
cat osx/docker-compose-Darwin-x86_64 > /usr/local/bin/docker-compose

echo "making docker-compose executable"
chmod +x /usr/local/bin/docker-compose
docker-compose -v || exit 0

echo "create a docker machine for the pipeline"
echo "you need to have at least 8096mb of ram available for your vm"

VIRTUALBOX_BOOT2DOCKER_URL=file:///$(pwd)/boot2docker.iso
export VIRTUALBOX_BOOT2DOCKER_URL
echo "VIRTUALBOX_BOOT2DOCKER_URL = ${VIRTUALBOX_BOOT2DOCKER_URL}"

VIRTUALBOX_MEMORY_SIZE="8192"
export VIRTUALBOX_MEMORY_SIZE
echo "VIRTUALBOX_MEMORY_SIZE = ${VIRTUALBOX_MEMORY_SIZE}"

docker-machine create -d virtualbox fluxcapacitor-pipeline

eval $(docker-machine env fluxcapacitor-pipeline)

# echo "check if you have the docker container already docker images |grep 8a642f29fd93"
echo "docker load < fluxcapacitor-pipeline.tar" 
docker load < fluxcapacitor-pipeline.tar
docker images

echo "If you need to delete your existing docker machine"
echo "docker-machine stop fluxcapacitor-pipeline"
echo "docker-machine rm fluxcapacitor-pipeline"

echo "Virtualbox version is $(vboxmanage --version)"
echo "if you are having problems with virtualbox you may need to uninstall or upgrade."
echo "/Volumes/$USB/VirtualBox_Uninstall.tool.sh"
docker-machine env fluxcapacitor-pipeline

echo "to run this image type the following"
echo "docker run -it fluxcapacitor/pipeline /bin/bash"
#docker tag 0875d176e1c6 fluxcapacitor/pipeline

docker-machine ls

echo "the ip address of your virtual machine is $(docker-machine ip fluxcapacitor-pipeline) this is handy if you have services running on it that you need to access."

echo "if you want to copy prepare a usbstick"
echo "rsync -r --delete --progress ../ /Volumes/$USB"

echo "execute this after you run the script as well since it won't stay in your shell with this script."
echo "# eval \$(docker-machine env fluxcapacitor-pipeline)"
