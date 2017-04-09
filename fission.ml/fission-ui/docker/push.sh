#!/bin/bash

set -e

user=$1
tag=$2
if [ -z "$user" ]
then
    user=fission
fi
if [ -z "$tag" ]
then
    tag=latest
fi

. build.sh

cp -r ../build ./fission-ui

docker build -t fission-ui .
docker tag fission-ui $user/fission-ui:$tag
docker push $user/fission-ui:$tag

rm -rf ./fission-ui
