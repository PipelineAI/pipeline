cd $MYAPPS_HOME/serving/flask/
nohup python recommendation-service.py > $LOGS_HOME/flask/flask.log &
echo '...tail -f $LOGS_HOME/flask/flask.log...' 
