## Prerequisites
### MySQL
* Follow [these](https://github.com/fluxcapacitor/sql.ml) instructions to setup MySQL

### (Optional) Spark
* Follow [these](https://github.com/fluxcapacitor/apachespark.ml) instructions to setup Spark

## Airflow
```
docker run -itd --name=airflow --net=host -e MYSQL_MASTER_SERVICE_HOST=<mysql-host-ip> -e MYSQL_MASTER_SERVICE_PORT_MYSQL_NATIVE=<mysql-port> -e SPARK_MASTER_2_0_1_SERVICE_HOST=<spark-master-host-ip> -e SPARK_MASTER_2_0_1_SERVICE_PORT_SPARK_SUBMIT=<spark-master-port> -e GITHUB_REPO_OWNER_URL="https://github.com/fluxcapacitor" -e GITHUB_REPO_NAME="source.ml" -e GITHUB_RESET_REVISION="HEAD" -e GITHUB_CLONE_BRANCH="master" -e GITHUB_CHECKOUT_BRANCH="master" -p 8080:8080 fluxcapacitor/scheduler-airflow
```
