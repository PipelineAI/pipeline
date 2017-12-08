# Distributed TensorFlow Training on Kubernetes in 1-Click
* These instructions are under active development
* We assume you already have a running Kubernetes cluster

## Build Docker Image
```
pipeline train-server-build --model-type=tensorflow --model-name=census --model-tag=v1 --model-path=./tensorflow/census/model/
```

## Push Image To Docker Repo
```
pipeline train-server-push --model-type=tensorflow --model-name=census --model-tag=v1
```

## Start Distributed TensorFlow Training Cluster
Notes:
* lack of `\ ` blank escapes
* `/root/ml/input/...` prepended to the `--train-files` and `--eval-files`
* different `.../data/...` dir structure than what would be on the host
```
pipeline train-cluster-start --model-type=tensorflow --model-name=census --model-tag=v1 --input-path=./tensorflow/census/input --output-path=./tensorflow/census/output --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=training/adult.training.csv --eval-files=validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025"
```

## Local Testing
```
cd ./tensorflow/census
```
```
python $PIPELINE_MODEL_PATH/pipeline_train.py --train-files=/root/ml/input/training/adult.training.csv --eval-files=/root/ml/input/validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025
```

## Cleaning Up
```
kubectl delete -f .pipeline-generated-train-cluster-tfserving-tensorflow-census-v1.yaml
```
