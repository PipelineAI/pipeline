# $1: old tag
# $2: new tag
find $PIPELINE_HOME -name Dockerfile -type f -exec sed -i -e 's/:master/:v1.0.0/g' {} \;
find $PIPELINE_HOME -name Dockerfile*.* -type f -exec sed -i -e 's/:master/:v1.0.0/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/:master/:v1.0.0/g' {} \;
find $PIPELINE_HOME -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "master"/revision: "v1.0.0"/g' {} \;
