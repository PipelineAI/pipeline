## Prerequisites
* MySQL
* Follow [these](https://github.com/fluxcapacitor/sql.ml) instructions to setup MySQL

## Presto Master
```
docker run -itd --name=presto-master --host=net -e MYSQL_MASTER_SERVICE_HOST=<mysql-host-ip> -e MYSQL_MASTER_SERVICE_PORT_MYSQL_NATIVE=<mysql-host-ip> fluxcapacitor/presto-master-0.145
```

## Presto Worker
```
docker run -itd --name=presto-worker --host=net -e PRESTO_MASTER_SERVICE_HOST=<presto-master-host-ip> -e PRESTO_MASTER_SERVICE_PORT_PRESTO_NATIVE=<presto-master-port> fluxcapacitor/presto-worker-0.145
```

## AirPal (Presto UI)
```
docker run -itd --name=airpal --host=net -e MYSQL_MASTER_SERVICE_PORT_MYSQL_NATIVE=<mysql-host-ip> -e PRESTO_MASTER_SERVICE_HOST=<presto-master-host-ip> -e PRESTO_MASTER_SERVICE_PORT_PRESTO_NATIVE=<presto-master-port> fluxcapacitor/airpal
```
