echo '$1: PIO_MODEL_STORE (ie. /path/to/model/)'=$1
echo '$2: PIO_MODEL_TYPE (ie. scikit, tensorflow, python3, spark, xgboost)'=$2
echo '$3: PIO_MODEL_NAME (ie. my_model)'=$3
echo '$4: (Optional): PIO_MODEL_SERVER_ALLOW_UPLOAD (default False)'=$4

docker run --name=predict -itd -m 4G -p 80:80 -p 10254:10254 -p 9876:9876 -p 9040:9040 -p 9090:9090 -p 3000:3000 -v $1:/root/model_store -e "PIO_MODEL_TYPE=$2" -e "PIO_MODEL_NAME=$3" -e "PIO_MODEL_SERVER_ALLOW_UPLOAD=$4" fluxcapacitor/predict-cpu:master
