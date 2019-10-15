curl http://chris.cloud.pipeline.ai/seldon/deployment/abtest-model/api/v0.1/predictions -d '{"data":{"names":["image_id"],"tensor":{"shape":[1,1],"values":[0]}}}' -H "Content-Type: application/json"
