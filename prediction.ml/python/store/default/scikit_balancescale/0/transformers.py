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
    transformed_data_list = []
    data_json = json.loads(data)

    transformed_data_list = ([parse_json(d) for d in data_json])
    return np.array(transformed_data_list)

def parse_json(d):
    return d['feature0'], d['feature1'], d['feature2'], d['feature3']

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
