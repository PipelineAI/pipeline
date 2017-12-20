# Nvidia's TensorRT 3.0+

## Convert TensorFlow Model into Optimized TensorRT File
```
pipeline predict-server-optimize --model-path=./tensorflow/mnist/model/model.pb \
                                 --model-name=mnist \
                                 --model-tag=a \
                                 --model-type=tensorflow \
                                 --model-runtime=tensorrt \
                                 --model-chip=gpu \
                                 --model-precision=float16 \
                                 --model-input-list=[...] \
                                 --model-output-list=[...]
                                

### EXPECTED OUTPUT ###
model path: './tensorflow/mnist/model/model.pb
model name: 'mnist'
model tag: 'a'
model type: 'tensorflow'
model runtime: 'tensorrt'
model chip: 'gpu'
model precision: 'float16'
model input list: '[...]'
model output list: '[...]'

genereated optimized model: './tensorflow/mnist/model/optimized_model.engine'
```

## Start TensorRT Runtime
```
pipeline predict-server-start --model-path=./tensorflow/mnist/model/optimized_model.engine \
                              --model-name=mnist \
                              --model-tag=a \
                              --model-type=tensorflow \
                              --model-runtime=tensorrt \
                              --model-chip=gpu \
                              --model-precision=float16 \
                              --model-input-list=[...] \
                              --model-output-list=[...]

### EXPECTED OUTPUT ###
model path: './tensorflow/mnist/model/model.pb
model name: 'mnist'
model tag: 'a'
model type: 'tensorflow'
model runtime: 'tensorrt'
model chip: 'gpu'
model precision: 'float16'
model input list: '[...]'
model output list: '[...]'

started 'tensorrt' model server at :8080/invocations
```
