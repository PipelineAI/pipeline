# TODO:  Actively working on this

## Build To Docker Repo
```
pipeline train-server-build --model-runtime=tfserving --model-type=tensorflow --model-name=census --model-tag=v1 --model-path=./tensorflow/census/
```

## Push To Docker Repo
```
pipeline train-server-push --model-runtime=tfserving --model-type=tensorflow --model-name=census --model-tag=v1
```

## Start Distributed TensorFlow Training Cluster
Notes:
* lack of `\ ` blank escapes
* `/root/input/...` prepended to the `--train-files` and `--eval-files`
* different `.../data/...` dir structure than what would be on the host
```
pipeline train-cluster-start --model-runtime=tfserving --model-type=tensorflow --model-name=census --model-tag=v1 --input-path=/root/model/tfserving/tensorflow/census/v1 --output-path=/root/model/tfserving/tensorflow/census/v1/versions --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=/root/model/tfserving/tensorflow/census/v1/data/train/adult.data.csv --eval-files=/root/model/tfserving/tensorflow/census/v1/data/eval/adult.test.csv --num-epochs=2 --learning-rate=0.025"
```

## Local Testing
```
python $PIPELINE_MODEL_PATH/pipeline_train.py --train-files=/root/model/tfserving/tensorflow/census/v1/data/train/adult.data.csv --eval-files=/root/model/tfserving/tensorflow/census/v1/data/eval/adult.test.csv --num-epochs=2 --learning-rate=0.025
```

## Cleaning Up
```
kubectl delete -f .pipeline-generated-train-cluster-tfserving-tensorflow-census-v1.yaml
```
