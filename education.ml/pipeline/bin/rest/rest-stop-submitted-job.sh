# If you're running this outside of the Docker container, 
# you will need to use port 36066 given the way we've mapped the ports from 
# Docker Host to Docker Container Guest

curl -X POST http://127.0.0.1:6066/v1/submissions/kill/$1

#{
#  "action" : "KillSubmissionResponse",
#  "message" : "Kill request for driver-20151008145126-0000 submitted",
#  "serverSparkVersion" : "1.6.1",
#  "submissionId" : "driver-20151008145126-0000",
#  "success" : true
#}
