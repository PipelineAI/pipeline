import logging
import os
import shutil
import subprocess
import json
import requests
from retrying import retry
import numpy as np

KFP_PACKAGE = 'https://storage.googleapis.com/ml-pipeline/release/0.1.20/kfp.tar.gz'
def notebook_setup():
  # Install the SDK

  subprocess.check_call(["pip3", "install", "-r", "requirements.txt"])
  subprocess.check_call(["pip3", "install", KFP_PACKAGE, "--upgrade"])

  logging.basicConfig(format='%(message)s')
  logging.getLogger().setLevel(logging.INFO)

  subprocess.check_call(["gcloud", "auth", "configure-docker", "--quiet"])
  subprocess.check_call(["gcloud", "auth", "activate-service-account",
                         "--key-file=" + os.getenv("GOOGLE_APPLICATION_CREDENTIALS"),
                         "--quiet"])

def copy_data_to_nfs(nfs_path, model_dir):
  if not os.path.exists(nfs_path):
    shutil.copytree("ames_dataset", nfs_path)

  if not os.path.exists(model_dir):
    os.makedirs(model_dir)

@retry(wait_exponential_multiplier=1000, wait_exponential_max=5000,
       stop_max_delay=2*60*1000)
def predict_nparray(url, data, feature_names=None):
  pdata = {
      "data": {
          "names":feature_names,
          "tensor": {
              "shape": np.asarray(data.shape).tolist(),
              "values": data.flatten().tolist(),
          },
      }
  }
  serialized_data = json.dumps(pdata)
  r = requests.post(url, data={'json':serialized_data}, timeout=5)
  return r
