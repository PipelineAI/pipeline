# $1: old tag
# $2: new tag
find $PIPELINE_HOME -name Dockerfile -type f -exec sed -i -e 's/:v1.0.0/:master/g' {} \;
find $PIPELINE_HOME -name Dockerfile*.* -type f -exec sed -i -e 's/:v1.0.0/:master/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/:v1.0.0/:master/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "v1.0.0"/revision: "master"/g' {} \;
