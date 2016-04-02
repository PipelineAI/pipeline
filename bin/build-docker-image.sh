# This is a convenience script to build the Docker image 

# cd to where the Dockerfile lives
# We're assuming this is ~/pipeline, but this might not be the case
cd ~/pipeline

echo "... *** MAKE SURE YOU ARE IN THE SAME DIRECTORY AS THE Dockerfile OR ELSE YOU WILL SEE AN ERROR *** ..."

docker build  -t fluxcapacitor/pipeline . 
