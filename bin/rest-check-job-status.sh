# If you're running this outside of the Docker container,
# you will need to use port 36066 given the way we've mapped the ports from
# Docker Host to Docker Container Guest

curl http://127.0.0.1:6066/v1/submissions/status/$1

#{
#  "action" : "SubmissionStatusResponse",
#  "driverState" : "FINISHED",
#  "serverSparkVersion" : "1.6.1",
#  "submissionId" : "driver-20151008145126-0000",
#  "success" : true,
#  "workerHostPort" : "192.168.3.153:46894",
#  "workerId" : "worker-20151007093409-192.168.3.153-46894"
#}
