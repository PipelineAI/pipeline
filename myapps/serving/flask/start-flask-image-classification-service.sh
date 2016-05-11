cd $MYAPPS_HOME/serving/flask/
nohup python image-classification-service.py > $LOGS_HOME/flask/image-classification-service.log &
echo '...tail -f $LOGS_HOME/flask/image-classification-service.log...' 
