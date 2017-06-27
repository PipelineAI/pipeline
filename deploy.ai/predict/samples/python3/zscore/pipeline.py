class Pipeline(object):

    def __init__(self,
                 model):
        self.model = model

        
    def predict(self,
                request):

        cat_affinity_score = sum([ d['weight'] * d['user_score'] for d in request if 'cat' in d['tags'] ])
        dog_affinity_score = sum([ d['weight'] * d['user_score'] for d in request if 'dog' in d['tags'] ])

        # create normalized z score for compare/classify
        cat_zscore = (cat_affinity_score - self.model['cat_mean'])/self.model['cat_stdv']
        dog_zscore = (dog_affinity_score - self.model['dog_mean'])/self.model['dog_stdv']

        # classify
        if abs(cat_zscore) > abs(dog_zscore):
            if cat_zscore >= 0:
                category = "cat_lover"
            else:
                category = "cat_hater"
        else:
            if dog_zscore >= 0:
                category = "dog_lover"
            else:
                category = "dog_hater"

        response = {
            'category': category,
            'cat_affinity_score': cat_affinity_score,
            'dog_affinity_score': dog_affinity_score,
            'cat_zscore': cat_zscore,
            'cat_zscore': dog_zscore
        }

        return response


    def transform_request(self,
                          request):
        import json        
        request_str = request.decode('utf-8')
        request_str = request_str.strip().replace('\n', ',')
        request_dict = json.loads(request_str)
        return request_dict
    
    def transform_response(self,
                           response):
        import json
        response_json = json.dumps(response)
        return response_json


if __name__ == '__main__':
    import cloudpickle as pickle

    cat_mean = 0.1
    cat_stdv = 0.20
    dog_mean = 0.3
    dog_stdv = 0.40

    model = {'cat_mean':cat_mean,
             'cat_stdv':cat_stdv,
             'dog_mean':dog_mean,
             'dog_stdv':dog_stdv}

    pipeline = Pipeline(model)

    pipeline_pkl_path = 'pipeline.pkl'

    with open(pipeline_pkl_path, 'wb') as fh:
        pickle.dump(pipeline, fh)
