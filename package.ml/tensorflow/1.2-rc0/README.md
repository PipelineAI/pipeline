For GPUs, you must build with `nvidia-docker` as follows:
```
nohup sudo nvidia-docker build --no-cache -t fluxcapacitor/package-tensorflow-1.2-rc0-gpu:master -f Dockerfile.gpu . &
```
