#echo '...Starting Redis...'
#nohup redis-server $REDIS_HOME/redis.conf &

echo '...Starting Config Service...'
cd $MYAPPS_HOME/serving/config
$MYAPPS_HOME/serving/config/start-config-service.sh

echo '...Starting Discovery Service...'
cd $MYAPPS_HOME/serving/discovery
$MYAPPS_HOME/serving/discovery/start-discovery-service.sh

echo '...Starting Hystrix Dashboard...'
nohup java -jar $HYSTRIX_DASHBOARD_HOME/standalone-hystrix-dashboard-$HYSTRIX_DASHBOARD_VERSION-all.jar > $LOGS_HOME/serving/hystrix/hystrix.log &

echo '...Starting Turbine Service...'
cd $MYAPPS_HOME/serving/turbine
$MYAPPS_HOME/serving/turbine/start-turbine-service.sh

echo '...Starting Atlas...'
nohup java -jar $ATLAS_HOME/atlas-$ATLAS_VERSION-standalone.jar $ATLAS_HOME/conf/atlas.conf > $LOGS_HOME/serving/atlas/atlas.log &

echo '...Starting TensorFlow Serving Inception Service...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service.sh

echo '...Starting TensorFlow Serving Inception Service Proxy...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service-proxy.sh

echo '...Starting TensorFlow Serving Inception Sidecar Inception Service...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-sidecar-service.sh

#echo '...Starting TensorFlow Serving Inception Service...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service.sh

#echo '...Starting TensorFlow Serving Inception Service Proxy...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service-proxy.sh

#echo '...Starting TensorFlow Serving Inception Sidecar Inception Service...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-sidecar-service.sh

#echo '...Starting Prediction Service...'
#echo ''
#echo '...This will take a minute or two...'
#echo ''
#echo '...***************************...'
#echo '...*** IGNORE ALL ERRORS!! ***...'
#echo '...***************************...'
#cd $MYAPPS_HOME/serving/prediction
#./start-prediction-service.sh
