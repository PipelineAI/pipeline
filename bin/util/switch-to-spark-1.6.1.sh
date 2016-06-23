export SPARK_VERSION=1.6.1

cd $PIPELINE_HOME

source config/bash/pipeline.bashrc

# Spark
echo '...Configuring Spark...'
mkdir -p $LOGS_HOME/spark/spark-events
ln -sf $CONFIG_HOME/spark/spark-defaults.conf $SPARK_HOME/conf
ln -sf $CONFIG_HOME/spark/spark-env.sh $SPARK_HOME/conf
ln -sf $CONFIG_HOME/spark/slaves $SPARK_HOME/conf
ln -sf $CONFIG_HOME/spark/metrics.properties $SPARK_HOME/conf
ln -sf $CONFIG_HOME/spark/fairscheduler.xml $SPARK_HOME/conf
ln -sf $CONFIG_HOME/spark/hive-site.xml $SPARK_HOME/conf
ln -sf $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib
