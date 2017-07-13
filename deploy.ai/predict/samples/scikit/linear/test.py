import cloudpickle as pickle

import pio_model

if __name__ == '__main__':
    with open('data/test_request.json', 'rb') as fh:
        request_binary = fh.read()
   
    response = pio_model.predict(request_binary)
    print(response)
