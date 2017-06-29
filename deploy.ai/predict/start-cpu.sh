echo '$1: PIO_MODEL_STORE (ie. /path/to/models/parent/path/)'==$PIO_MODEL_STORE
echo '$2: PIO_MODEL_TYPE (ie. scikit, tensorflow, python3, spark, xgboost)'==$PIO_MODEL_TYPE
echo '$3: PIO_MODEL_NAME (ie. my_model)'==$PIO_MODEL_NAME

docker run --name=deploy-predict-cpu -itd -m 4G -p 6969:6969 -p 7070:7070 -p 10254:10254 -p 9876:9876 -p 9040:9040 -p 9090:9090 -p 3000:3000 -v $PIO_MODEL_STORE:/root/model_store -e "PIO_MODEL_TYPE=$PIO_MODEL_TYPE" -e "PIO_MODEL_NAME=$PIO_MODEL_NAME" fluxcapacitor/deploy-predict-cpu:master
