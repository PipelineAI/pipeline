# Copyright 2016 Google Inc. All Rights Reserved.
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
FROM gcr.io/tensorflow/tensorflow:latest-devel

RUN pip install --upgrade pip
RUN apt-get update
RUN apt-get install -y unzip python-dev python-pip zlib1g-dev libjpeg-dev libblas-dev
RUN apt-get install -y liblapack-dev libatlas-base-dev libsnappy-dev libyaml-dev gfortran
RUN apt-get install -y python-scipy

RUN pip install sklearn nltk pillow setuptools
RUN pip install flask google-api-python-client
RUN pip install pandas python-snappy scipy scikit-learn requests uritemplate
RUN pip install --upgrade --force-reinstall https://storage.googleapis.com/cloud-ml/sdk/cloudml.latest.tar.gz

# RUN python -c "import nltk; nltk.download('punkt')"

RUN curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-138.0.0-linux-x86_64.tar.gz | tar xvz
RUN ./google-cloud-sdk/install.sh -q
RUN ./google-cloud-sdk/bin/gcloud components install beta

ADD download_git_repo.py download_git_repo.py

# TensorBoard
EXPOSE 6006
# IPython
EXPOSE 8888
# Flask
EXPOSE 5000

CMD ["sh", "-c", "python download_git_repo.py ; /bin/bash"]
