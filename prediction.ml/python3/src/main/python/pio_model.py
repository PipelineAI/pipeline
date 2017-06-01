### `PioModelInitializer` Class
# Must implement the `initialize_model()` method.
class PioModelInitializer(object):
    def __init__(self,
                 *args,
                 **kwargs):

        pass


    def initialize_model(self,
                        *args,
                        **kwargs):

        return


### `PioRequestTransformer` Class
#Must implement the `transform_request()` method.
class PioRequestTransformer(object):
    def __init__(self,
                 *args,
                 **kwargs):
        pass


    def transform_request(self,
                          request,
                          *args,
                          **kwargs):
        return request


### `PioResponseTransformer` Class
# Must implement the `transform_response()` method.
class PioResponseTransformer(object):
    def __init__(self,
                 *args,
                 **kwargs):
        pass


    def transform_response(self,
                           response,
                           *args,
                           **kwargs):
        return response


### `PioModel` Class
# Must implement the `predict()` method.
class PioModel(object):

    def __init__(self,
                 request_transformer,
                 response_transformer,
                 model_initializer,
                 *args,
                 **kwargs):

        self.request_transformer = request_transformer
        self.response_transformer = response_transformer

        self.model_initializer = model_initializer
        self.model = self.model_initializer.initialize_model(args,
                                                             kwargs)


    def predict(self,
                request,
                *args,
                **kwargs):

        return
