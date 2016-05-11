cd $MYAPPS_HOME/serving/flask/
nohup python imageclassifier-service.py > $LOGS_HOME/flask/image-classifier-service.log &
echo '...tail -f $LOGS_HOME/flask/image-classifier-service.log...' 
