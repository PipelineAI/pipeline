import os
os.environ['KERAS_BACKEND'] = 'theano'
os.environ['THEANO_FLAGS'] = 'floatX=float32,device=cpu'

import cloudpickle as pickle
import pipeline_predict
import pandas as pd
import numpy as np
import keras
from keras.layers import Input, Dense
from keras.models import Model
from keras.models import save_model, load_model

from sklearn.preprocessing import StandardScaler, MinMaxScaler, Normalizer

if __name__ == '__main__':
    df = pd.read_csv("data/training.csv")
    df["People per Television"] = pd.to_numeric(df["People per Television"],errors='coerce')
    df = df.dropna()

    x = df["People per Television"].values.reshape(-1,1).astype(np.float64)
    y = df["People per Physician"].values.reshape(-1,1).astype(np.float64)

    # min-max -1,1
    sc = MinMaxScaler(feature_range=(-1,1))

    x_ = sc.fit_transform(x)
    y_ = sc.fit_transform(y)

    inputs = Input(shape=(1,))
    preds = Dense(1,activation='linear')(inputs)

    model = Model(inputs=inputs,outputs=preds)
    sgd = keras.optimizers.SGD()
    model.compile(optimizer=sgd ,loss='mse')
    model.fit(x_,y_, batch_size=1, verbose=1, epochs=10, shuffle=False)

    save_model(model, 'state/keras_theano_linear_model_state.h5')

    model_pkl_path = 'model.pkl' 

    with open(model_pkl_path, 'wb') as fh:
        pickle.dump(pipeline_predict, fh)
