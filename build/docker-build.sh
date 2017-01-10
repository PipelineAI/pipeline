cd $PIPELINE_HOME

# package.ml
sudo docker build -q -t fluxcapacitor/package-ubuntu-14.04 -f package.ml/ubuntu/14.04/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-anaconda-4.1.11 -f package.ml/anaconda/4.1.11/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-java-openjdk-1.8 -f package.ml/java/openjdk/1.8/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-java-oracle-1.8 -f package.ml/java/oracle/1.8/Dockerfile .
# HACK sudo docker build -q -t fluxcapacitor/package-spark-2.0.1 -f package.ml/spark/2.0.1/Dockerfile .
cd package.ml/spark/2.0.1/ && sudo docker build -q -t fluxcapacitor/package-spark-2.0.1 .
cd $PIPELINE_HOME

sudo docker build -q -t fluxcapacitor/package-kafka-0.8 -f package.ml/kafka/0.8/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-kafka-0.10 -f package.ml/kafka/0.10/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-presto-0.145 -f package.ml/presto/0.145/Dockerfile .
sudo docker build -q -t fluxcapacitor/package-apache2 -f package.ml/apache2/Dockerfile .

# apachespark.ml
sudo docker build -q -t fluxcapacitor/apachespark-master-2.0.1 -f apachespark.ml/2.0.1/Dockerfile.master .
sudo docker build -q -t fluxcapacitor/apachespark-worker-2.0.1 -f apachespark.ml/2.0.1/Dockerfile.worker .

# cassandra.ml
sudo docker build -q -t fluxcapacitor/cassandra -f cassandra.ml/Dockerfile .

# clustered.ml
sudo docker build -q -t fluxcapacitor/clustered-tensorflow -f clustered.ml/tensorflow/Dockerfile .

# dashboard.ml
sudo docker build -q -t fluxcapacitor/dashboard-hystrix -f dashboard.ml/hystrix/Dockerfile .
sudo docker build -q -t fluxcapacitor/dashboard-turbine -f dashboard.ml/turbine/Dockerfile .

# elasticsearch.ml
sudo docker build -q -t fluxcapacitor/elasticsearch-2.3.0 -f elasticsearch.ml/2.3.0/Dockerfile .

# gpu.ml
sudo docker build -q -t fluxcapacitor/gpu -f gpu.ml/Dockerfile .

# jupyterhub.ml
sudo docker build -q -t fluxcapacitor/jupyterhub -f jupyterhub.ml/Dockerfile .

# keyvalue.ml
sudo docker build -q -t fluxcapacitor/keyvalue-redis -f keyvalue.ml/redis/Dockerfile .

# kibana.ml
# HACK sudo docker build -q -t fluxcapacitor/package-spark-2.0.1 -f package.ml/spark/2.0.1/Dockerfile .
#sudo docker build -q -t fluxcapacitor/kibana-4.5.0 -f kibana.ml/4.5.0/Dockerfile .
cd kibana.ml/4.5.0 && sudo docker build -q -t fluxcapacitor/kibana-4.5.0 .
cd $PIPELINE_HOME

# kubernetes.ml
sudo docker build -q -t fluxcapacitor/kubernetes -f kubernetes.ml/Dockerfile .
sudo docker build -q -t fluxcapacitor/kubernetes-admin -f kubernetes.ml/Dockerfile.admin .

# loadtest.ml
sudo docker build -q -t fluxcapacitor/loadtest -f loadtest.ml/Dockerfile .

# metastore.ml
sudo docker build -q -t fluxcapacitor/metastore-1.2.1 -f metastore.ml/Dockerfile .

# prediction.ml
sudo docker build -q -t fluxcapacitor/prediction-codegen -f prediction.ml/codegen/Dockerfile .
sudo docker build -q -t fluxcapacitor/prediction-keyvalue -f prediction.ml/keyvalue/Dockerfile .
sudo docker build -q -t fluxcapacitor/prediction-pmml -f prediction.ml/pmml/Dockerfile .
sudo docker build -q -t fluxcapacitor/prediction-tensorflow -f prediction.ml/tensorflow/Dockerfile .

# presto.ml
sudo docker build -q -t fluxcapacitor/presto-master-0.145 -f presto.ml/presto-master/Dockerfile .
sudo docker build -q -t fluxcapacitor/presto-worker-0.145 -f presto.ml/presto-worker/Dockerfile .

# scheduler.ml
sudo docker build -q -t fluxcapacitor/scheduler-airflow -f scheduler.ml/airflow/Dockerfile .

# sql.ml
sudo docker build -q -t fluxcapacitor/sql-mysql -f sql.ml/mysql/Dockerfile .

# web.ml
sudo docker build -q -t fluxcapacitor/web-home -f web.ml/home/Dockerfile .

# zeppelin.ml
sudo docker build -q -t fluxcapacitor/zeppelin -f zeppelin.ml/Dockerfile .

# zookeeper.ml
sudo docker build -q -t fluxcapacitor/zookeeper -f zookeeper.ml/Dockerfile .
