class Pipeline(object):

    def __init__(self):
        pass
        
    def predict(self,
                request):
        pass


    def transform_request(self,
                          request):
        import tensorflow as tf
        import json
        import numpy as np
        request_str = request.decode('utf-8')
        request_json = json.loads(request_str)
        request_np = np.asarray([request_json['x_observed']])
        return request_np
         
    
    def transform_response(self,
                           response):
        import json
        return json.dumps({"y_pred": response.tolist()[0]})


if __name__ == '__main__':
    import cloudpickle as pickle

    pipeline = Pipeline()

    pipeline_pkl_path = 'pipeline.pkl'

    with open(pipeline_pkl_path, 'wb') as fh:
        pickle.dump(pipeline, fh)
