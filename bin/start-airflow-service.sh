cd $PIPELINE_HOME

echo '...Starting Airflow...'
nohup airflow webserver &
