# References:
#  https://towardsdatascience.com/an-intuitive-guide-to-deep-network-architectures-65fdc477db41
#  https://scikit-learn.org/stable/modules/generated/sklearn.metrics.pairwise_distances.html

import numpy as np
import pandas as pd
import os
import time
import random
import json

from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications.xception import Xception
from tensorflow.keras.applications.xception import preprocess_input

from sklearn.neighbors import DistanceMetric

images_path = 'Images/'
batch_size = 200
img_w_size = 299
img_h_size = 299

print('** LOADING IMAGES **')
datagen = ImageDataGenerator(preprocessing_function=preprocess_input)
image_generator = datagen.flow_from_directory(
            images_path,
            target_size=(img_w_size, img_h_size),
            batch_size=batch_size,
            class_mode=None,
            shuffle=False)
# Note:  This needs to stay up here because we use it later to resolve the image name
images = image_generator.next()
print('** LOADED IMAGES **')

# TODO: Convert this to a dict instead of an array
#       See TODO's below for supporting tasks
pairwise_top_25 = {} 


def train(num_result_images=25):
    # Convert 2D image matrix => 1D bottleneck vector
    print('\n** GENERATING BOTTLENECKS bottlenecks.csv **')

    # Setup model to convert 2D image matrix => 1D bottleneck vector
    base_model = Xception(include_top=False,
                          weights='imagenet',
                          input_shape=(img_w_size, img_h_size, 3),
                          pooling='avg')

    bottlenecks = base_model.predict(images)

    # TODO:  Change this to json
    np.savetxt("bottleneck.csv", bottlenecks, delimiter=",") 
    print('\n** GENERATED BOTTLENECKS to bottleneck.csv **')

    bottlenecks = np.loadtxt("bottleneck.csv", delimiter=",")

    print('\n** GENERATING PAIRWISE pairwise_top_25.json **')
    dist = DistanceMetric.get_metric('euclidean')

    # Calculate pairwise distance -- O(n^2)
    bottleneck_pairwise_dist = dist.pairwise(bottlenecks)

    # Find the top 100 similar images per image
    retrieved_images = []
    for image_idx in range(0, len(bottleneck_pairwise_dist)):
        retrieved_indexes = pd.Series(bottleneck_pairwise_dist[image_idx]).sort_values().head(num_result_images).index.tolist()
        retrieved_indexes_int = list(map(lambda index: int(index), retrieved_indexes))

        pairwise_top_25[image_idx] = retrieved_indexes_int

    with open('pairwise_top_25.json', 'w') as fp:
        json.dump(pairwise_top_25, fp)

    print('\n** GENERATED PAIRWISE to pairwise_top_25.json **')


if __name__== "__main__":
    start = time.time()
    print("Start time: ", start)
    try:
        train()
    finally:
        end = time.time()
        print("End time: ", end)
        print("Total time taken: ", (end - start))
    # test
    image_idx = 0
    top_25_images = pairwise_top_25[image_idx]
    print('\n** MOST SIMILAR IMAGES **')
    print(top_25_images)
