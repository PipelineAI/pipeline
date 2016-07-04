echo '...Starting Config Service...'
cd $MYAPPS_HOME/serving/config
$MYAPPS_HOME/serving/config/start-config-service.sh

echo '...Starting Discovery Service...'
cd $MYAPPS_HOME/serving/discovery
$MYAPPS_HOME/serving/discovery/start-discovery-service.sh

echo '...Starting Hystrix Dashboard...'
nohup java -jar $HYSTRIX_DASHBOARD_HOME/standalone-hystrix-dashboard-$HYSTRIX_DASHBOARD_VERSION-all.jar > $LOGS_HOME/serving/hystrix/hystrix.log &

echo '...Starting Turbine Service...'
cd $MYAPPS_HOME/serving/metrics/turbine
$MYAPPS_HOME/serving/metrics/turbine/start-turbine-service.sh

echo '...Starting Atlas...'
nohup java -jar $ATLAS_HOME/atlas-$ATLAS_VERSION-standalone.jar $ATLAS_HOME/conf/atlas.conf > $LOGS_HOME/serving/atlas/atlas.log &

echo '...Starting TensorFlow Inception...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-inception-serving-service.sh

echo '...Starting Flask-based Image Classifier...'
$MYAPPS_HOME/serving/flask/start-flask-image-classification-service.sh

echo '...Starting Prediction Service...'
cd $MYAPPS_HOME/serving/prediction
sbt assembly
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
