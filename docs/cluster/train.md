# Distributed TensorFlow Training
* These instructions are under active development
* We assume you already have a running Kubernetes cluster

# Build Docker Image
```
pipeline train-server-build --model-name=census --model-tag=a --model-type=tensorflow --model-path=./tensorflow/census/model/
```

# Push Image To Docker Repo
```
pipeline train-server-push --model-name=census --model-tag=a --model-type=tensorflow 
```

# Kubernetes
## Start Distributed TensorFlow Training Cluster
Notes:
* lack of `\ ` blank escapes
* `/root/ml/input/...` prepended to the `--train-files` and `--eval-files`
* different `.../data/...` dir structure than what would be on the host
```
pipeline train-kube-start --model-name=census --model-tag=a --model-type=tensorflow --input-path=./tensorflow/census/input --output-path=./tensorflow/census/output --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=training/adult.training.csv --eval-files=validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025"
```

# AWS SageMaker
## Start Distributed TensorFlow Training Cluster
# TODO: Coming Soon
```
pipeline train-sage-start --model-name=census --model-tag=a --model-type=tensorflow --input-path=./tensorflow/census/input --output-path=./tensorflow/census/output --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=training/adult.training.csv --eval-files=validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025"
```
