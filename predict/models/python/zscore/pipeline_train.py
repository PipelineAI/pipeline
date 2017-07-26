import cloudpickle as pickle

if __name__ == '__main__':

    cat_mean = 0.1
    cat_stdv = 0.20
    dog_mean = 0.3
    dog_stdv = 0.40

    model = {'cat_mean':cat_mean,
             'cat_stdv':cat_stdv,
             'dog_mean':dog_mean,
             'dog_stdv':dog_stdv}

    model_pkl_path = 'model.pkl'

    with open(model_pkl_path, 'wb') as fh:
        pickle.dump(model, fh)
