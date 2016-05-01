# This is a convenience script to build the Docker image 

# cd to where the Dockerfile lives
# We're assuming this is $PIPELINE_HOME (/root/pipeline), 
#   but this might not be the case
cd $PIPELINE_HOME

echo '... *** MAKE SURE YOU ARE IN THE SAME DIRECTORY AS THE Dockerfile OR ELSE YOU WILL SEE AN ERROR *** ...'
nohup docker build  -t fluxcapacitor/pipeline . && docker push fluxcapacitor/pipeline & 
