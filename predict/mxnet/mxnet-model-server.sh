#!/bin/bash

# Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License.
# A copy of the License is located at
#     http://www.apache.org/licenses/LICENSE-2.0
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.

GUNICORN_ARGS='gunicorn'
MXNET_ARGS='mxnet'
NGINX_ARGS='nginx'
MMS_ARG='mms'
NGINX_CONFIG_FILE='/etc/nginx/conf.d/virtual.conf'
MMS_CONFIG_FILE=''

# Start MxNet model server. This function expects one argument which is the application config file.
start_mms() 
{
    gunicorn_arguments=''
    # TODO issue:https://github.com/awslabs/mxnet-model-server/issues/337. Check if MMS is already running.

    # This function expects one argument.
    if [[ "$#" != 1 ]]
    then
        echo "ERROR: Wrong arguments given ($#) \"$@\""
        exit 1
    fi

    MMS_CONFIG_FILE=$1
    models=""
    models_found=0

    echo "INFO: Reading file $MMS_CONFIG_FILE"

    # if the MMS CONFIG FILE is an empty string or if the file doesn't exist, throw error and exit
    if [[ ( -z "${MMS_CONFIG_FILE##*( )}" ) || ( ! -f "$MMS_CONFIG_FILE" ) ]]
    then
        echo "ERROR: No configuration file given... $MMS_CONFIG_FILE"
        exit 1
    fi

    while read -r line || [ -n "$line" ]
    do
        line="${line##*( )}"
	    shopt -s nocasematch
	    # Comments and empty lines should be ignored . # and $ are comment starters.
	    if [[ ( -z "$line" ) || ( "$line" =~ ^\# ) || ( "$line" =~ ^\$ ) ]]
     	    then
    		continue
	    fi
	    # If a line starts with '[' treat it as a header
	    if [[ "$line" =~ ^\[[A-Za-z\ ] ]]
	    then
    		HEADER=$line
	        if [[ ( "$HEADER" =~ "$NGINX_ARGS" ) ]] ; then
	            rm -f $NGINX_CONFIG_FILE
	            touch $NGINX_CONFIG_FILE
	        fi
	        continue
	    fi
	    
	    if [[ "$HEADER" =~ "$GUNICORN_ARGS" ]] # Gunicorn args
	    then
    		gunicorn_arguments="${gunicorn_arguments} $line "
    	    elif [[ "$HEADER" =~ "$MXNET_ARGS" ]] # MxNet Engine Args
    	    then
    		export $line
    	    elif [[ "$HEADER" =~ "$NGINX_ARGS" ]] # Nginx Args
    	    then
    	    # MXNET_MODEL_SERVER_HOST gets updated when you run Docker (run/exec) command with the environment
    	    #    variables MXNET_MODEL_SERVER_HOST=$HOSTNAME
  	        if [[ ( "$line" =~ "server_name" ) && ( ! -z ${MXNET_MODEL_SERVER// } ) ]]
   	        then
   	            host_name=$MXNET_MODEL_SERVER_HOST
   	            line="server_name $host_name;"
   	        fi
    		echo "$line" >> $NGINX_CONFIG_FILE
            elif [[ "$HEADER" =~ "$MMS_ARG" ]] # MMS Args
            then
		if [[ "$line" =~ "--model" ]] ; then
                    models_found=1
		elif [[ $models_found == 1 ]] ; then
                    models=$line
                    models_found=0
		fi
    	    else
    		echo "ERROR: Invalid config header seen $HEADER"
    		exit 1
    	    fi
	    
    done < "$MMS_CONFIG_FILE"
    shopt -u nocasematch
    
    app_script='wsgi'
    service nginx restart
    # Download and extract all the models
    python /mxnet_model_server/setup_mms.py $models
    # If successful run gunicorn
    if [[ `echo $?` == 0 ]] ; then
        gunicorn $gunicorn_arguments --chdir /mxnet_model_server --env MXNET_MODEL_SERVER_CONFIG=$MMS_CONFIG_FILE $app_script
    fi
    
    rm -f /mxnet_model_server/.models
    echo "INFO: Ending the program"
}

stop_mms() {
    echo "Stopping MMS"
    kill -9 $(ps -e | grep "gunicorn\|nginx" | awk '{print $1}') 2> /dev/null
}

usage_help() { 
    echo "Usage:"
    echo ""
    echo "$0 [start | stop | restart | help] [--mms-config <MMS config file>]"
    echo ""
    echo "start        : Start a new instance of MXNet model server."
    echo "stop         : Stop the current running instance of MXNet model server"
    echo "restart      : Restarts all the MXNet Model Server worker instances."
    echo "help         : Usage help for $0"
    echo "--mms-config : Location pointing to the MXNet model server configuration file."
    echo "To start the MXNet model server, run"
    echo "$0 start --mms-config <path-to-config-file>"
    echo ""
    echo "To stop the running instance of MXNet model server, run"
    echo "$0 stop"
    echo ""
    echo "To restart the running instance of MXNet model server, run"
    echo "$0 restart --mms-config <path-to-config-file>"
}

main() {
    option='none'
    mms_config_file='default'
    
    shopt -s nocasematch
    while true ; do
        case "$1" in
    	    "start" | "stop" | "help" | "restart" ) option=$1 ; shift ;;
	    "--mms-config" )  mms_config_file=$2 ; shift 2 ;;
	    * ) break ;;
        esac
    done
    
    case "$option" in
        "start" ) start_mms $mms_config_file ;;
        "stop" ) stop_mms ;;
        "restart" ) stop_mms && sleep 5 && start_mms $mms_config_file ;;
        "help" ) usage_help ;;
        "none" | "*")
            echo "Invalid options given"
            usage_help
            exit 1;;
    esac
    
    shopt -u nocasematch
}

main "$@"
exit $?
