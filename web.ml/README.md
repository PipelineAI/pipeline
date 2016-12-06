```
docker run -it --name=web-apache --privileged --net=host -p 80:80 fluxcapacitor/web-apache
```

## Reference
* Build new Docker Image
```
docker build -t fluxcapacitor/web-apache .
```
