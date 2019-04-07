#!/bin/bash 
  #-e

# Python 2
echo "Python Version: "
python2 -V

pip2 uninstall -y cli-pipeline 

rm -rf build/
rm -rf dist/
rm -rf cli_pipeline.egg-info
rm -rf cli_pipeline/__pycache__

#pip2 install mypy --ignore-installed --no-cache --upgrade 

# Only runs in python3
#mypy cli_pipeline/__init__.py
#mypy cli_pipeline/cli_pipeline.py

# Pinned to the incorrect versions on purpose
# Testing if --ignore-installed is working
pip2 install python-dateutil==2.7.2 --ignore-installed --no-cache --upgrade 
pip2 install boto3==1.7.3 --ignore-installed --no-cache --upgrade 

pip2 install -r requirements.txt --ignore-installed --no-cache --upgrade
python2 setup.py install 
pip2 install -e . --ignore-installed --no-cache --upgrade

pipeline version

pip2 list

# Python 3
echo "Python Version: "
python3 -V

pip3 uninstall -y cli-pipeline

rm -rf build/
rm -rf dist/
rm -rf cli_pipeline.egg-info
rm -rf cli_pipeline/__pycache__

pip3 install mypy --ignore-installed --no-cache --upgrade

# Pinned to the incorrect versions on purpose
# Testing if --ignore-installed is working
pip3 install python-dateutil==2.7.2 --ignore-installed --no-cache --upgrade
pip3 install boto3==1.7.3 --ignore-installed --no-cache --upgrade

pip3 install -r requirements.txt --ignore-installed --no-cache --upgrade
python3 setup.py install
pip3 install -e . --ignore-installed --no-cache --upgrade
pip3 list

mypy cli_pipeline/__init__.py
mypy cli_pipeline/cli_pipeline.py
pipeline version

# TODO: Conda [Python2,Python3], Pip [9.0.3,10.0.0]
#pip install pip==10.0.0 --ignore-installed --no-cache --upgrade
#pip3 install pip==10.0.0 --ignore-installed --no-cache --upgrade

#wget -q https://repo.continuum.io/miniconda/Miniconda3-4.4.10-Linux-x86_64.sh -O /tmp/miniconda.sh && \
#    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
#    /opt/conda/bin/conda install --yes python=3.6 pip=10.0.0 && \
#    rm /tmp/miniconda.sh
#export PATH=/opt/conda/bin:$PATH
#echo "export PATH=/opt/conda/bin:$PATH" >> /root/.bashrc
#echo "export PATH=/opt/conda/bin:$PATH" >> /etc/environment

pipeline cluster-kube-install --tag 1.5.0 --ingress-type=loadbalancer --chip=cpu --namespace=default --image-registry-url=docker.io --image-registry-username=username --image-registry-password=password --ui-gateway=app-gateway.default.svc.cluster.local --api-gateway=app-gateway.default.svc.cluster.local --users-storage-gb=100Gi --dry-run=True

