#!/bin/bash
# This script will create a docker-machine image for the pipeline project
# Author Benjamin Rizkowsky benoahriz@gmail.com 

ask_yes_or_no() {
    read -p "$1 ([y]es or [N]o): "
    case $(echo $REPLY | tr '[A-Z]' '[a-z]') in
        y|yes) echo "yes" ;;
        *)     echo "no" ;;
    esac
}

# if [[ "no" == $(ask_yes_or_no "This will overwrite your current docker binaries with known to work with versiosn. Are you sure?") ]]
# then
#     echo "Skipped."
#     exit 0
# fi

OSXDIR="osx"
IMAGEHASH="aeab7cca3c8c"

echo "check the version of docker-machine"
docker-machine -v || echo "somethings wrong"

echo "check the version of docker"
docker -v || echo "somethings wrong"

echo "check the version of docker-compose"
docker-compose -v || echo "somethings wrong"

echo "create a docker machine for the pipeline"
echo "you need to have at least 5120mb of ram available for your vm"

VIRTUALBOX_BOOT2DOCKER_URL=file:///$(pwd)/${OSXDIR}/boot2docker.iso
export VIRTUALBOX_BOOT2DOCKER_URL
echo "VIRTUALBOX_BOOT2DOCKER_URL = ${VIRTUALBOX_BOOT2DOCKER_URL}"

VIRTUALBOX_MEMORY_SIZE="5120"
export VIRTUALBOX_MEMORY_SIZE
echo "VIRTUALBOX_MEMORY_SIZE = ${VIRTUALBOX_MEMORY_SIZE}"

VIRTUALBOX_CPU_COUNT="4"
export VIRTUALBOX_CPU_COUNT
echo "VIRTUALBOX_CPU_COUNT = ${VIRTUALBOX_CPU_COUNT}"


docker-machine create -d virtualbox pipelinebythebay 2>/dev/null|| echo "Sweet! you already have a pipelinebythebay docker-machine!"
eval $(docker-machine env pipelinebythebay)

 
if [[ $(docker images |grep ${IMAGEHASH}) ]]; then
	echo "already have the image ${IMAGEHASH}"
else
	echo "docker load < pipelinebythebay.tar"
	docker load < pipelinebythebay.tar
fi

docker images |grep ${IMAGEHASH} && echo "looks like you successfully loaded the docker image :)"

echo "If you need to delete your existing docker machine"
echo "docker-machine stop pipelinebythebay && docker-machine rm pipelinebythebay"

echo "To run this image type the following"
echo "docker run -it 0875d176e1c6 /bin/bash"
docker tag -f ${IMAGEHASH} bythebay/pipeline
echo "alternatively docker run -it bythebay/pipeline /bin/bash"

docker-machine ls |grep pipelinebythebay
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "the ip address of your virtual machine is $(docker-machine ip pipelinebythebay) this is handy if you have services running on it that you need to access."

echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "if you want to copy prepare a usbstick, this is only what I use on my mac YMMV"
echo "rsync -r --delete --progress ../ /volumes/KINGSTON"

echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "execute this after you run this script since you need the DOCKER_HOST vars to persist."
echo "eval \$(docker-machine env pipelinebythebay)"
