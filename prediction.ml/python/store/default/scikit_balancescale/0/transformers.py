from __future__ import print_function, absolute_import, division

import six
import socket
import time
import struct
import ujson as json
import pandas as pd
import numpy as np

# input: raw json
# output: numpy array
def input_transformer(data): 
    df = pd.io.json.read_json(data)
    array = df.values
    return array

# input: numpy array
# output: list of json   
def output_transformer(data):
    return json.dumps(data.tolist())

if __name__ == '__main__':
    input_str = '[{"feature0":0, "feature1":1, "feature2":2, "feature3":3}, \
                  {"feature0":10, "feature1":11, "feature2":12, "feature3":13}]'

    transformed_input = input_transformer(input_str)
    print(transformed_input)

    transformed_output = output_transformer(transformed_input)
    print(transformed_output)
