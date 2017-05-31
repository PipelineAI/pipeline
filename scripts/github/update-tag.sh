#!/bin/bash

# $1: old tag
# $2: new tag
echo "PIO_HOME="$PIO_HOME
echo ""
echo "Don't forget to tag both pipeline/ and source.ml/ !!"
find $PIPELINE_HOME -name Dockerfile -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $PIPELINE_HOME -name Dockerfile*.* -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
