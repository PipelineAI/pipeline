cd $MYAPPS_HOME/serving/flask/
nohup python tf-model-update-service.py > $LOGS_HOME/serving/flask/flask-tf-model-update-service.log &
echo '...tail -f $LOGS_HOME/serving/flask/flask-tf-model-update-service.log...' 
