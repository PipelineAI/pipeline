import cloudpickle as pickle

import model

if __name__ == '__main__':
    with open('data/test_request.json', 'rb') as fh:
        request_binary = fh.read()
   
    response = model.predict(request_binary)
    print(response)
