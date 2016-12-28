# Stop Spark Job - not Deterministic, so use with caution!
jps | grep "SparkSubmit" | cut -d " " -f "1" | xargs kill -KILL
