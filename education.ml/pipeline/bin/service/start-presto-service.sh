cd $PIPELINE_HOME

echo '...Starting Presto...'
nohup launcher --data-dir=$WORK_HOME/presto --launcher-log-file=$LOGS_HOME/presto/launcher.log --server-log-file=$LOGS_HOME/presto/presto.log start
