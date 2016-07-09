cd $MYAPPS_HOME/serving/tensorflow/
nohup python tensorflow-inception-serving-service-proxy.py > $LOGS_HOME/serving/tensorflow/inception-serving-service-proxy.log &
echo '...tail -f $LOGS_HOME/serving/tensorflow/inception-serving-service-proxy.log...' 
