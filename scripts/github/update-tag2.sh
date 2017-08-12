#!/bin/bash

# $1: old tag
# $2: new tag
echo "SOURCE_HOME="$SOURCE_HOME
echo ""
find $SOURCE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $SOURCE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "'$1'"/revision: "'$2'"/g' {} \;
find $SOURCE_HOME -name *-deploy*.yaml -type f -exec sed -i -e 's/':"$1"'/':"$2"'/g' {} \;
find $SOURCE_HOME -name *-deploy*.yaml -type f -exec sed -i -e 's/revision: "'$1'"/revision: "'$2'"/g' {} \;
