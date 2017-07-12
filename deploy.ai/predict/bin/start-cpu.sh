echo 'PIO_MODEL_TYPE (ie. scikit, tensorflow, python3, spark, xgboost)'==$PIO_MODEL_TYPE
echo 'PIO_MODEL_NAME (ie. my_model)'==$PIO_MODEL_NAME

docker run --name=deploy-predict-$PIO_MODEL_TYPE-$PIO_MODEL_NAME-cpu -itd -m 4G -p 6969:6969 -p 7070:7070 -p 10254:10254 -p 9876:9876 -p 9040:9040 -p 9090:9090 -p 3000:3000 -e "PIO_MODEL_TYPE=$PIO_MODEL_TYPE" -e "PIO_MODEL_NAME=$PIO_MODEL_NAME" fluxcapacitor/deploy-predict-$PIO_MODEL_TYPE-$PIO_MODEL_NAME-cpu:master
