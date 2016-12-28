# If you're running this outside of the Docker container,
# you will need to use port 36066 given the way we've mapped the ports from
# Docker Host to Docker Container Guest

curl -X POST http://127.0.0.1:6066/v1/submissions/create --header "Content-Type:application/json;charset=UTF-8" --data '{
  "action" : "CreateSubmissionRequest",
  "appArgs" : [ "10" ],
  "appResource" : "file:/root/spark-1.6.1-bin-fluxcapacitor/lib/spark-examples-1.6.1-hadoop2.6.0.jar",
  "clientSparkVersion" : "1.6.1",
  "environmentVariables" : {
    "SPARK_ENV_LOADED" : "1"
  },
  "mainClass" : "org.apache.spark.examples.SparkPi",
  "sparkProperties" : {
    "spark.jars" : "file:/root/spark-1.6.1-bin-fluxcapacitor/lib/spark-examples-1.6.1-hadoop2.6.0.jar",
    "spark.driver.supervise" : "false",
    "spark.app.name" : "SparkPi",
    "spark.submit.deployMode" : "cluster",
    "spark.master" : "spark://127.0.0.1:7077",
    "spark.executor.cores" : "1",
    "spark.executor.memory" : "512m",
    "spark.cores.max" : "4",
    "spark.eventLog.enabled" : "true",
    "spark.eventLog.dir" : "/root/pipeline/logs/spark",
    "spark.history.fs.logDirectory" : "logs/spark/spark-events",
    "spark.sql.parquet.filterPushdown" : "true",
    "spark.sql.parquet.cacheMetadata" : "true",
    "spark.sql.inMemoryColumnarStorage.partitionPruning" : "true",
    "spark.sql.codegen" : "true",
    "spark.sql.unsafe.enabled" : "true"
  }
}'

#{
#  "action" : "CreateSubmissionResponse",
#  "message" : "Driver successfully submitted as driver-20151008145126-0000",
#  "serverSparkVersion" : "1.6.1",
#  "submissionId" : "driver-20151008145126-0000",
#  "success" : true
#}
