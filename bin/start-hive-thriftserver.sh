echo '...Starting Apache Spark JDBC ODBC Hive ThriftServer...'

cd $PIPELINE_HOME

# MySql must be started - and the password set - before ThriftServer will startup
# Starting the ThriftServer will create a dummy derby.log and metastore_db per https://github.com/apache/spark/pull/6314
# The actual Hive metastore defined in conf/hive-site.xml is still used, however.
nohup $SPARK_HOME/sbin/start-thriftserver.sh \
  --jars $SPARK_SUBMIT_JARS \
  --packages $SPARK_SUBMIT_PACKAGES \
  --hiveconf hive.server2.thrift.port=10000 \
  --hiveconf hive.server2.thrift.bind.host=0.0.0.0 \
  --master spark://127.0.0.1:7077
