import dill as pickle

class Predictor(object):

    def __init__(self):
        pass

 
    def setup(self): 
        pass


    def predict(self, inputs):
        return 'response' 

if __name__ == '__main__':
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('model_pkl_filename')
    args = parser.parse_args()
    model_pkl_filename = args.model_pkl_filename

    print("Training model...")
    print("...Done!")

    print("Pickling model to '%s'..." % model_pkl_filename)
    predictor = Predictor()
    with open(model_pkl_filename, 'wb') as model_pkl_file:
        pickle.dump(predictor, model_pkl_file)
    print("...Done!")
