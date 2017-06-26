class PioBundle(object):

    def __init__(self,
                 model):
        self.model = model

        
    def predict(self,
                request):

        return self.model.predict(request)


    def transform_request(self,
                          request):
        import ujson
        import numpy as np
        request_str = request.decode('utf-8')
        request_str = request_str.strip().replace('\n', ',')
        # surround the json with '[' ']' to prepare for conversion
        request_str = '[%s]' % request_str
        request_json = ujson.loads(request_str)
        request_transformed = ([json_line['feature0'] for json_line in request_json])
        return np.array(request_transformed)   

    
    def transform_response(self,
                           response):
        import ujson
        return ujson.dumps(response.tolist())


if __name__ == '__main__':
    import numpy as np
    from sklearn import linear_model
    from sklearn import datasets

    # Load the diabetes dataset
    diabetes = datasets.load_diabetes()

    # ONLY USING 1 FEATURE FOR THIS EXAMPLE!
    # Use only one feature
    diabetes_X = diabetes.data[:, np.newaxis, 2]

    # Split the data into training/testing sets
    diabetes_X_train = diabetes_X[:-20]
    diabetes_X_test = diabetes_X[-20:]

    # Split the targets into training/testing sets
    diabetes_y_train = diabetes.target[:-20]
    diabetes_y_test = diabetes.target[-20:]

    # Create linear regression model
    model = linear_model.LinearRegression()

    # Train the model using the training sets
    model.fit(diabetes_X_train, diabetes_y_train)

    import dill as pickle

    pio_bundle = PioBundle(model)

    pio_bundle_pkl_path = 'pio_bundle.pkl'

    with open(pio_bundle_pkl_path, 'wb') as fh:
        pickle.dump(pio_bundle, fh)
