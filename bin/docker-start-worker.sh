docker exec pipeline$1 bash && nohup /root/spark-1.6.1-bin-fluxcapacitor/sbin/start-slave.sh --cores 2 --memory 2048m --webui-port 6061 -h 172.17.0.$1 spark://172.17.0.1:7080

