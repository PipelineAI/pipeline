cd ~/pipeline

git pull

mkdir -p ~/.ipython/profile_default/startup
cp ~/scripts/fixme/00-setup-spark.py ~/.ipython/profile_default/startup

hadoop fs -copyFromLocal ~/datasets /

stop-slave.sh

SPARK_WORKER_WEBUI_PORT=46061 $SPARK_HOME/sbin/start-slave.sh --cores 4 --memory 2g -h 0.0.0.0 spark://127.0.0.1:47077 &
