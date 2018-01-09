Build TensorFlow Model CPU
```
pipeline predict-server-build --model-name=mnist --model-tag=cpu --model-type=tensorflow --model-path=./tensorflow/mnist-cpu/model --model-chip=cpu
```

Build TensorFlow Model GPU
```
pipeline predict-server-build --model-name=mnist --model-tag=gpu --model-type=tensorflow --model-path=./tensorflow/mnist-gpu/model --model-chip=gpu
```

Push TensorFlow Model CPU
```
pipeline predict-server-push --model-name=mnist --model-tag=cpu
```

Push TensorFlow Model GPU
```
pipeline predict-server-push --model-name=mnist --model-tag=gpu
```
