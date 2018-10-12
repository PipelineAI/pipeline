from mms.model_loader import ModelLoader
import json
import sys

# Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License.
# A copy of the License is located at
#     http://www.apache.org/licenses/LICENSE-2.0
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.


def write_models_to_file(models=None):
    """
    Read model's metadata returned from the ModelLoader and write it into a file to be read from the
    gunicorn workers
    :param models: models metadata (service name, model dir, manifest file ...)
    :return: void
    """
    mxnet_model_metadata_file = "/mxnet_model_server/.models"
    if models is None:
        sys.exit(1)

    print("INFO: Writing models metadata...")
    with (open(mxnet_model_metadata_file, 'w')) as fp:
        json.dump(models, fp)


def main():
    """
    This API downloads and extracts the models before instantiating the MMS
    :return:
    """

    model_list = []
    models = ' '
    models = models.join(sys.argv[1:])

    print("INFO: Getting model files {}\n".format(models))
    model_list += models.split(' ')
    models_dict = ModelLoader.load(dict(s.split('=') for s in model_list))
    write_models_to_file(models_dict)


if __name__ == "__main__":
    main()

