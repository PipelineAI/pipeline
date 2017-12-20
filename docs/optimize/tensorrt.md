# Nvidia's TensorRT 3.0+

## Convert Unoptimized TensorFlow Model into UFF (used by TensorRT)
```
pipeline predict-server-optimize --model-path=./tensorflow/mnist/model --model-name=mnist --model-tag=a --model-type=tensorflow --model-optimize-type=uff
```

## Start TensorRT Runtime
```
pipeline predict-server-start --model-path=./tensorflow/mnist/model --model-name=mnist --model-tag=a --model-type=uff --model-runtime=tensorrt --model-chip-gpu 
```
