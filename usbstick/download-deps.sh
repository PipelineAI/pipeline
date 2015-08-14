#!/bin/bash
# This script will download the docker binaries for working on OSX 
# Author Benjamin Rizkowsky benoahriz@gmail.com 


OSXDIR="osx"
WINDOWSDIR="windows"

if [ -d ${OSXDIR} ]; then
  echo "dir already exists"
else 
  echo "creating directory ${OSXDIR}"
  mkdir ${OSXDIR}
fi

if [ -d ${windows} ]; then
  echo "dir already exists"
else 
  echo "creating directory ${windows}"
  mkdir ${windows}
fi


# Boot2Docker
curl -L "https://github.com/boot2docker/boot2docker/releases/download/v1.8.0/boot2docker.iso" > ${OSXDIR}/boot2docker.iso

# Docker Toolbox
curl -L "https://github.com/docker/toolbox/releases/download/v1.8.0a/DockerToolbox-1.8.0a.pkg" > ${OSXDIR}/DockerToolbox-1.8.0a.pkg
curl -L "https://github.com/docker/toolbox/releases/download/v1.8.0b/DockerToolbox-1.8.0b.exe" >  ${WINDOWSDIR}/DockerToolbox-1.8.0b.exe

# Virtualbox
curl -L "http://download.virtualbox.org/virtualbox/5.0.0/VirtualBox-5.0.0-101573-OSX.dmg" > ${OSXDIR}/VirtualBox-5.0.0-101573-OSX.dmg
curl -L "http://download.virtualbox.org/virtualbox/5.0.0/Oracle_VM_VirtualBox_Extension_Pack-5.0.0-101573.vbox-extpack" > ${OSXDIR}/Oracle_VM_VirtualBox_Extension_Pack-5.0.0-101573.vbox-extpack
curl -L "https://www.virtualbox.org/download/hashes/5.0.0/MD5SUMS" > ${OSXDIR}/MD5SUMS.virtualbox

# echo "Showing the md5 sums of the files"
# find ${OSXDIR} -type f -exec md5 -r {} \;

echo "assuming you already have a docker-machine to save out the pipeline image for offlines usage"
echo "you will need to run docker pull bythebay/pipeline"
echo "then you need to run docker save bythebay/pipeline > pipelinebythebay.tar"






