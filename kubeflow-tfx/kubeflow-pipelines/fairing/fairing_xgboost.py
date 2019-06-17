
# Copyright 2018 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import logging
import pandas as pd
from sklearn.metrics import mean_absolute_error
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import Imputer
from xgboost import XGBRegressor
import urllib.request


TRAINING_URL="https://raw.githubusercontent.com/kubeflow/examples/master/xgboost_ames_housing/ames_dataset/train.csv"
TRAINING_FILE="train.csv"

ESTIMATORS=1000
LEARNING_RATE=0.1
TEST_FRACTION_SIZE=0.25
EARLY_STOPPING_ROUNDS=50

def run_training_and_eval():
    (train_X, train_y), (test_X, test_y) = read_input()
    model = train_model(train_X,
                        train_y,
                        test_X,
                        test_y,
                        ESTIMATORS,
                        LEARNING_RATE)

    eval_model(model, test_X, test_y)

def download(url, file_name):
    with urllib.request.urlopen(url) as response, open(file_name, "wb") as file:
        file.write(response.read())

def read_input(test_size=TEST_FRACTION_SIZE):
    """Read input data and split it into train and test."""
    download(TRAINING_URL, TRAINING_FILE)
    data = pd.read_csv(TRAINING_FILE)
    data.dropna(axis=0, subset=['SalePrice'], inplace=True)

    y = data.SalePrice
    X = data.drop(['SalePrice'], axis=1).select_dtypes(exclude=['object'])

    train_X, test_X, train_y, test_y = train_test_split(X.values,
                                                        y.values,
                                                        test_size=test_size,
                                                        shuffle=False)

    imputer = Imputer()
    train_X = imputer.fit_transform(train_X)
    test_X = imputer.transform(test_X)

    return (train_X, train_y), (test_X, test_y)

def train_model(train_X,
                train_y,
                test_X,
                test_y,
                n_estimators,
                learning_rate):
    """Train the model using XGBRegressor."""
    model = XGBRegressor(n_estimators=n_estimators,
                      learning_rate=learning_rate)

    model.fit(train_X,
              train_y,
              early_stopping_rounds=EARLY_STOPPING_ROUNDS,
              eval_set=[(test_X, test_y)])

    logging.info("Best RMSE on eval: %.2f with %d rounds",
                 model.best_score,
                 model.best_iteration+1)
    return model

def eval_model(model, test_X, test_y):
    """Evaluate the model performance."""
    predictions = model.predict(test_X)
    logging.info("mean_absolute_error=%.2f", mean_absolute_error(predictions, test_y))


import fairing
fairing.config.set_builder('append', base_image='tensorflow/tensorflow:1.13.1-py3', registry='pipelineai/fairing_xgboost', push=False)
#fairing.config.set_deployer('job')
run_training_and_eval = fairing.config.fn(run_training_and_eval)
run_training_and_eval()
