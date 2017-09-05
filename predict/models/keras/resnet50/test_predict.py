import pipeline_predict

if __name__ == '__main__':
    with open('data/test_request.json', 'rb') as fh:
        request_binary = fh.read()
   
    response = pipeline_predict.predict(request_binary)
    print(response)
    response = pipeline_predict.predict(request_binary)
    print(response)

