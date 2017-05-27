#!/bin/bash

# $1: old tag
# $2: new tag
echo "PIPELINE_HOME="$PIPELINE_HOME
echo "And don't forget to tag source.ml!"
find $PIPELINE_HOME -name Dockerfile -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $PIPELINE_HOME -name Dockerfile*.* -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "'$1'"/revision: "'$2'"/g' {} \;
find $PIPELINE_HOME -name *-deploy*.yaml -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $PIPELINE_HOME -name *-deploy*.yaml -type f -exec sed -i -e 's/revision: "'$1'"/revision: "'$2'"/g' {} \;
