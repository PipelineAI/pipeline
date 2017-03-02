## Introduction

The minimal TensorFlow application for benchmarking.

## Start predict server

```
./tensorflow_model_server --port=9000 --model_name=minimal --model_base_path=./model
```

## Start predict client

```
./predict_client.py --host 127.0.0.1 --port 9000 --model_name minial --model_version 1
```

```
cloudml models predict -n minial -s 127.0.0.1:9000 -f ./data.json
```

## Benchmark

### Test local predict

```
./benchmark_predict.py --benchmark_test_number 10 --benchmark_batch_size 1
```

### Test python gRPC client

```
./benchmark_predict_client.py --host 127.0.0.1 --port 9000 --model_name minial --model_version 1 --benchmark_batch_size 100  --benchmark_test_number 10000
```
