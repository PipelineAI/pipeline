echo '...Starting Config Service...'
cd $MYAPPS_HOME/serving/config
$MYAPPS_HOME/serving/config/start-config-service.sh

echo '...Starting Discovery Service...'
cd $MYAPPS_HOME/serving/discovery
$MYAPPS_HOME/serving/discovery/start-discovery-service.sh

echo '...Starting Hystrix Dashboard...'
nohup java -jar $HYSTRIX_DASHBOARD_HOME/standalone-hystrix-dashboard-$HYSTRIX_DASHBOARD_VERSION-all.jar > $LOGS_HOME/serving/hystrix/hystrix.log &

echo '...Starting Atlas...'
nohup java -jar $ATLAS_HOME/atlas-$ATLAS_VERSION-standalone.jar $ATLAS_HOME/conf/atlas.conf > $LOGS_HOME/serving/atlas/atlas.log &

echo '...Starting Prediction Service...'
# TODO:  Start this in the background with nohup &
cd $MYAPPS_HOME/serving
./start-serving-worker.sh
#sbt package
#java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
