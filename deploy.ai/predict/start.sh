echo '$1: PIO_MODEL_STORE (ie. /path/to/model/)'
echo '$2: PIO_MODEL_TYPE (ie. scikit, tensorflow, spark, xgboost)'
echo '$3: PIO_MODEL_NAME (ie. my_model)'
echo '$4 (Optional): PIO_MODEL_SERVER_ALLOW_UPLOAD (default False)'

docker run --name=predict -itd -m 4G -p 80:80 -p 10254:10254 -p 9876:9876 -p 9040:9040 -v $1:/root/model_store -e "PIO_MODEL_TYPE=$2" -e "PIO_MODEL_NAME=$3" -e "PIO_MODEL_SERVER_ALLOW_UPLOAD=$4" fluxcapacitor/predict-cpu:master
