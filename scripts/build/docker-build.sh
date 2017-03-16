cd $PIPELINE_HOME

# TODO:  Add --no-cache

# package.ml
cd $PIPELINE_HOME/package.ml/ubuntu/14.04/ && sudo docker build -q -t fluxcapacitor/package-ubuntu-14.04 .
cd $PIPELINE_HOME/package.ml/ubuntu/16.04/ && sudo docker build -q -t fluxcapacitor/package-ubuntu-16.04 .
cd $PIPELINE_HOME/package.ml/anaconda/4.1.11/ && sudo docker build -q -t fluxcapacitor/package-anaconda-4.1.11 .
cd $PIPELINE_HOME/package.ml/java/openjdk/1.8/ && sudo docker build -q -t fluxcapacitor/package-java-openjdk-1.8 .
cd $PIPELINE_HOME/package.ml/java/oracle/1.8/ && sudo docker build -q -t fluxcapacitor/package-java-oracle-1.8 .
cd $PIPELINE_HOME/package.ml/spark/2.1.0/ && sudo docker build -q -t fluxcapacitor/package-spark-2.1.0 .

cd $PIPELINE_HOME/package.ml/kafka/0.8/ && sudo docker build  -q -t fluxcapacitor/package-kafka-0.8 .
cd $PIPELINE_HOME/package.ml/kafka/0.10/ && sudo docker build -q -t fluxcapacitor/package-kafka-0.10 .
cd $PIPELINE_HOME/package.ml/presto/0.167/ && sudo docker build -q -t fluxcapacitor/package-presto-0.167 .
cd $PIPELINE_HOME/package.ml/apache2/ && sudo docker build -q -t fluxcapacitor/package-apache2 .

# apachespark.ml
cd $PIPELINE_HOME/apachespark.ml/2.0.1/ && sudo docker build -q -t fluxcapacitor/apachespark-master-2.0.1 -f Dockerfile.master .
cd $PIPELINE_HOME/apachespark.ml/2.0.1/ && sudo docker build -q -t fluxcapacitor/apachespark-worker-2.0.1 -f Dockerfile.worker .

# cassandra.ml
cd $PIPELINE_HOME/cassandra.ml && sudo docker build -q -t fluxcapacitor/cassandra .

# clustered.ml
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker build -q -t fluxcapacitor/clustered-tensorflow .

# dashboard.ml
cd $PIPELINE_HOME/dashboard.ml/hystrix && sudo docker build -q -t fluxcapacitor/dashboard-hystrix .
cd $PIPELINE_HOME/dashboard.ml/turbine && sudo docker build -q -t fluxcapacitor/dashboard-turbine .

# education.ml
#cd $PIPELINE_HOME/education.ml && sudo docker build -q -t fluxcapacitor/pipeline .
#cd $PIPELINE_HOME/education.ml/serving && sudo docker build -q -t fluxcapacitor/education-serving .

# elasticsearch.ml
cd $PIPELINE_HOME/elasticsearch.ml/2.3.0 && sudo docker build -q -t fluxcapacitor/elasticsearch-2.3.0 .

# gpu.ml
cd $PIPELINE_HOME/gpu.ml && sudo docker build -q -t fluxcapacitor/gpu-tensorflow .

# hdfs.ml
cd $PIPELINE_HOME/hdfs.ml && sudo docker build -q -t fluxcapacitor/hdfs .

# jupyterhub.ml
cd $PIPELINE_HOME/jupyterhub.ml && sudo docker build -q -t fluxcapacitor/jupyterhub .

# keyvalue.ml
cd $PIPELINE_HOME/keyvalue.ml/redis && sudo docker build -q -t fluxcapacitor/keyvalue-redis .

# kibana.ml
cd $PIPELINE_HOME/kibana.ml/4.5.0 && sudo docker build -q -t fluxcapacitor/kibana-4.5.0 .

# kubernetes.ml
cd $PIPELINE_HOME/kubernetes.ml && sudo docker build -q -t fluxcapacitor/kubernetes .
cd $PIPELINE_HOME/kubernetes.ml && sudo docker build -q -t fluxcapacitor/kubernetes-cli -f Dockerfile.cli .

# loadtest.ml
cd $PIPELINE_HOME/loadtest.ml && sudo docker build -q -t fluxcapacitor/loadtest .

# metastore.ml
cd $PIPELINE_HOME/metastore.ml && sudo docker build -q -t fluxcapacitor/metastore-1.2.1 .

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/codegen && sudo docker build -q -t fluxcapacitor/prediction-codegen .
cd $PIPELINE_HOME/prediction.ml/keyvalue && sudo docker build -q -t fluxcapacitor/prediction-keyvalue .
cd $PIPELINE_HOME/prediction.ml/pmml && sudo docker build -q -t fluxcapacitor/prediction-pmml .
cd $PIPELINE_HOME/prediction.ml/python && sudo docker build -q -t fluxcapacitor/prediction-python .
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker build -q -t fluxcapacitor/prediction-tensorflow .

# presto.ml
cd $PIPELINE_HOME/presto.ml/presto-master && sudo docker build -q -t fluxcapacitor/presto-master-0.167 .
cd $PIPELINE_HOME/presto.ml/presto-worker && sudo docker build -q -t fluxcapacitor/presto-worker-0.167 .
cd $PIPELINE_HOME/presto.ml/presto-ui && sudo docker build -q -t fluxcapacitor/presto-ui .

# scheduler.m l
cd $PIPELINE_HOME/scheduler.ml/airflow && sudo docker build -q -t fluxcapacitor/scheduler-airflow .

# sql.ml
cd $PIPELINE_HOME/sql.ml/mysql && sudo docker build -q -t fluxcapacitor/sql-mysql .

# web.ml
cd $PIPELINE_HOME/web.ml/home && sudo docker build -q -t fluxcapacitor/web-home .
cd $PIPELINE_HOME/web.ml/sparkafterdark && sudo docker build -q -t fluxcapacitor/web-sparkafterdark .

# zeppelin.ml
cd $PIPELINE_HOME/zeppelin.ml && sudo docker build -q -t fluxcapacitor/zeppelin .

# zookeeper.ml
cd $PIPELINE_HOME/zookeeper.ml && sudo docker build -q -t fluxcapacitor/zookeeper .
