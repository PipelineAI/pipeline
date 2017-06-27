curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  -F "file=@pipeline.tar.gz" \
  http://$PIO_MODEL_SERVER_URL/api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_ID

curl -X POST -H "Content-Type: [request_mime_type]" -d '[request_body]' https://[model_server_url]/api/v1/model/predict/[model_type]/[model_name]

./start.sh ~/workspace-fluxcapacitor/pipeline/deploy.ai/predict/samples/python3/zscore/ python3 zscore True

pio deploy --model_type python3 --model_name zscore --model_path .
pio predict --model_type python3 --model_name zscore --model-test-request-path ./data/test_request.json


./start.sh ~/workspace-fluxcapacitor/pipeline/deploy.ai/predict/samples/scikit/linear/ scikit linear True

pio deploy --model_type scikit --model_name linear --model_path .
pio predict --model_type scikit --model_name linear --model-test-request-path ./data/test_request.json


./start.sh ~/workspace-fluxcapacitor/pipeline/deploy.ai/predict/samples/tensorflow/linear/ tensorflow linear True

pio deploy --model_type tensorflow --model_name linear --model_path .
pio predict --model_type tensorflow --model_name linear --model-test-request-path ./data/test_request.json
