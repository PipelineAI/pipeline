#######################################################################
# Note:  These services need to start AFTER the Config Service starts
#        or they will fast fail.  This is intentional.
#######################################################################
#echo '...Starting TensorFlow Serving Inception Service...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service.sh

#echo '...Starting TensorFlow Serving Inception Service Proxy...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service-proxy.sh

#echo '...Starting TensorFlow Serving Inception Sidecar Inception Service...'
#$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-sidecar-service.sh

cd $MYAPPS_HOME/serving/prediction
./start-prediction-service.sh
