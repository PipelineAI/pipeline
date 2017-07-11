import cloudpickle as pickle

if __name__ == '__main__':
    model_pkl_path = 'model.pkl'

    # Load pickled model from model directory
    with open(model_pkl_path, 'rb') as fh:
        restored_model = pickle.load(fh)

    with open('data/test_request.json', 'rb') as fh:
        request_binary = fh.read()

    response = restored_model.predict(request_binary)
    print(response)
