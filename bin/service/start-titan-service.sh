cd $PIPELINE_HOME

echo '...Starting Titan...'
nodetool enablethrift
nohup $TITAN_HOME/bin/gremlin-server.sh $TITAN_HOME/conf/gremlin-server/gremlin-server-rest-modern.yaml &
