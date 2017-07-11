import cloudpickle as pickle
import model 

if __name__ == '__main__':
    model_pkl_path = 'model.pkl' 

    with open(model_pkl_path, 'wb') as fh:
        pickle.dump(model, fh)
