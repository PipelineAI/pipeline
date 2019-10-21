import keras
import sys
import numpy as np
import json

class DoppelgangerModel(object):
    def __init__(self):
        print("\n** LOADING MODEL from pairwise_top_25.json **")
        with open('pairwise_top_25.json') as fp:
            self.model = json.load(fp) 
        print("\n** LOADED MODEL from pairwise_top_25.json **")

    def predict(self, X, feature_names):
        similar_image_arr = self.model[str(int(X[0]))]
        return similar_image_arr

if __name__== "__main__":
    model = DoppelgangerModel()
    print(model.predict([0], ['image_id']))
