#!/bin/bash

echo "Args:"
echo " 1: branch or tag (ie. master, v1.2.0)"
echo ""
echo "branch=$1"
echo ""
echo "^^ These should not be empty!"
echo ""

docker run -itd --name jupyterhub -m 8G --env JUPYTERHUB_AUTHENTICATOR=dummyauthenticator.DummyAuthenticator --env OAUTH_CALLBACK_URL='' --env OAUTH_CLIENT_ID='' --env OAUTH_CLIENT_SECRET='' -p 80:80 -p 50070:50070 -p 39000:39000 -p 9000:9000 -p 9001:9001 -p 9002:9002 -p 9003:9003 -p 9004:9004 -p 6006:6006 -p 8754:8754 -p 7077:7077 -p 6066:6066 -p 6060:6060 -p 6061:6061 -p 4040:4040 -p 4041:4041 -p 4042:4042 -p 4043:4043 -p 4044:4044 -p 2222:2222 -p 10254:10254 fluxcapacitor/jupyterhub-cpu:$1
