cd $MYAPPS_HOME/serving/flask/
nohup python recommendation-service.py > $LOGS_HOME/serving/flask/flask-recommendations.log &
echo '...tail -f $LOGS_HOME/serving/flask/flask-recommendations.log...' 
