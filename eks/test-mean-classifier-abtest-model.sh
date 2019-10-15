curl   http://<ip-address>/seldon/deployment/abtest-model/api/v0.1/predictions   -d '{"data":{"names":["a","b"],"tensor":{"shape":[2,2],"values":[0,0,1,1]}}}'   -H "Content-Type: application/json"
