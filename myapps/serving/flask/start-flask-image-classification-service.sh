cd $TENSORFLOW_SERVING_HOME
nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_inference --port=9091 $DATASETS_HOME/tensorflow/serving/inception_model > nohup-inception.out &

cd $MYAPPS_HOME/serving/flask/
nohup python image-classification-service.py > $LOGS_HOME/flask/image-classification-service.log &
echo '...tail -f $LOGS_HOME/flask/image-classification-service.log...' 
