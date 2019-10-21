curl http://c0198e9d-istiosystem-istio-2af2-1928351968.eu-central-1.elb.amazonaws.com/seldon/deployment/doppelganger-model/api/v0.1/predictions -d '{"data":{"ndarray":[[0]]}}' -H "Content-Type: application/json"

# curl http://c0198e9d-istiosystem-istio-2af2-1928351968.eu-central-1.elb.amazonaws.com/seldon/deployment/doppelganger-model/api/v0.1/predictions -d '{"data":{"names":["id"],"tensor":{"shape":[1,1],"values":[0]}}}' -H "Content-Type: application/json"
