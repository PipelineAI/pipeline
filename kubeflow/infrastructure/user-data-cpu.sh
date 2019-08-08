#!/bin/bash

apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
add-apt-repository \
   "deb https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
   $(lsb_release -cs) \
   stable"
apt-get update && apt-get install -y --allow-downgrades docker-ce=$(apt-cache madison docker-ce | grep 18.09 | head -1 | awk '{print $3}')


# Note:  If we use this, we need to modify `docker.service`
#        or create `/etc/systemd/docker.service.d/docker.root.conf`
#        See https://github.com/IronicBadger/til/blob/master/docker/change-docker-root.md for more details
# Note:  We may need to use the deprecated overlay storage-driver because of the following issue that we see with certain linux kernels: https://github.com/moby/moby/issues/28391
# Be sure to pin your version of OS/kernel - especially when using cloud images that are constantly being patched/updated
cat > /etc/docker/daemon.json <<EOF
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

mkdir -p /mnt/pipelineai
# mount -o discard,defaults /dev/sdb /mnt/pipelineai
# echo UUID=`sudo blkid -s UUID -o value /dev/sdb` /mnt/pipelineai ext4 discard,defaults,nofail 0 2 | sudo tee -a /etc/fstab

mkdir -p /etc/systemd/system/docker.service.d/
cat > /etc/systemd/system/docker.service.d/docker.root.conf <<EOF
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -g /mnt/pipelineai -H fd://
EOF

# Verify that Docker Root Dir is /mnt/pipelineai
systemctl daemon-reload
systemctl restart docker
docker info

# Latest git
add-apt-repository ppa:git-core/ppa
apt-get update
apt-get install -y git

# Pin normal pip and pip3 to 10.0.1
pip install pip==10.0.1 --ignore-installed --no-cache --upgrade
pip3 install pip==10.0.1 --ignore-installed --no-cache --upgrade

# Pin Miniconda3 to 4.5.1 and pip to 10.0.1
wget -q https://repo.continuum.io/miniconda/Miniconda3-4.5.1-Linux-x86_64.sh -O /tmp/miniconda.sh && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.6 pip=10.0.1 && \
    rm /tmp/miniconda.sh
export PATH=/opt/conda/bin:$PATH
echo "export PATH=/opt/conda/bin:$PATH" >> /root/.bashrc
echo "export PATH=/opt/conda/bin:$PATH" >> /etc/environment

export HOME=/root

curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
deb https://apt.kubernetes.io/ kubernetes-xenial main
EOF
apt-get update
apt-get remove -y --allow-change-held-packages kubelet kubeadm kubectl
# Ksonnet doesn't seem to handle kube 1.15 very well
apt-get install -y kubelet=1.14.3-00 kubeadm=1.14.3-00 kubectl=1.14.3-00
#apt-mark hold kubelet kubeadm kubectl
apt autoremove

mkdir -p /mnt/pipelineai/kubelet

#kubeadm reset --force
#iptables -F && iptables -t nat -F && iptables -t mangle -F && iptables -X

# PipelineAI CLI
# NOTE:  WE NEED TO KEEP THIS UP HERE SINCE WE USE `pipeline` NEXT
export PIPELINE_CLI_VERSION=1.5.330
echo "export PIPELINE_CLI_VERSION=$PIPELINE_CLI_VERSION" >> /root/.bashrc
echo "export PIPELINE_CLI_VERSION=$PIPELINE_CLI_VERSION" >> /etc/environment
pip install cli-pipeline==$PIPELINE_CLI_VERSION --ignore-installed --no-cache --upgrade

# PipelineAI Runtime
export PIPELINE_VERSION=1.5.0
echo "export PIPELINE_VERSION=$PIPELINE_VERSION" >> /root/.bashrc
echo "export PIPELINE_VERSION=$PIPELINE_VERSION" >> /etc/environment

# Note:  we need to do a dry-run to generate the /root/.pipelineai/cluster/yaml/ and /root/.pipelineai/kube/
pipeline cluster-kube-install --tag $PIPELINE_VERSION --chip=cpu --namespace=kubeflow --image-registry-url=gcr.io/pipelineai2 --users-storage-gb=200Gi --ingress-type=nodeport --users-root-path=/mnt/pipelineai/users --dry-run

# Change root-dir to /mnt/pipelineai/kubelet
#cp /root/.pipelineai/kube/10-kubeadm.conf /etc/systemd/system/kubelet.service.d/10-kubeadm.conf
sed -i '0,/\[Service\]/a Environment="KUBELET_EXTRA_ARGS=--root-dir=/mnt/pipelineai/kubelet --feature-gates=DevicePlugins=true,BlockVolume=true"' /etc/systemd/system/kubelet.service.d/10-kubeadm.conf

systemctl daemon-reload
systemctl restart kubelet

# Note:  This command depends on the success of pipeline cluster-kube-install above!
kubeadm init --config=/root/.pipelineai/cluster/config/kubeadm-init.yaml

mkdir -p $HOME/.kube
cp --force /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id -u):$(id -g) $HOME/.kube/config

export KUBECONFIG=/root/.kube/config
echo "export KUBECONFIG=/root/.kube/config" >> /root/.bashrc
echo "export KUBECONFIG=/root/.kube/config" >> /etc/environment

# Setup Networking
kubectl apply -f "https://cloud.weave.works/k8s/net?k8s-version=$(kubectl version | base64 | tr -d '\n')"

# Allow the master to host pods
kubectl taint nodes --all node-role.kubernetes.io/master-

sleep 5

kubectl create namespace kubeflow

# Set Default Namespace
kubectl config set-context \
    $(kubectl config current-context) \
    --namespace kubeflow

# OpenEBS CRD/Operator and StorageClass
kubectl create -f https://openebs.github.io/charts/openebs-operator-0.9.0.yaml
sleep 30
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30
kubectl create -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30 
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30
kubectl create -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml

# Tab Completion
echo "source <(kubectl completion bash)" >> ~/.bashrc
source ~/.bashrc

export KSONNET_VERSION=0.13.1
echo "export KSONNET_VERSION=$KSONNET_VERSION" >> /root/.bashrc
echo "export KSONNET_VERSION=$KSONNET_VERSION" >> /etc/environment
wget https://github.com/ksonnet/ksonnet/releases/download/v${KSONNET_VERSION}/ks_${KSONNET_VERSION}_linux_amd64.tar.gz
tar -xzvf ks_${KSONNET_VERSION}_linux_amd64.tar.gz
mv ks_${KSONNET_VERSION}_linux_amd64/ks /usr/bin/

export KFCTL_VERSION=0.5.1
echo "export KFCTL_VERSION=$KFCTL_VERSION" >> /root/.bashrc
echo "export KFCTL_VERSION=$KFCTL_VERSION" >> /etc/environment
wget https://github.com/kubeflow/kubeflow/releases/download/v${KFCTL_VERSION}/kfctl_v${KFCTL_VERSION}_linux.tar.gz
tar -xzvf kfctl_v${KFCTL_VERSION}_linux.tar.gz
mv kfctl /usr/bin/

export KF_PIPELINES_VERSION=0.1.21
echo "export KF_PIPELINES_VERSION=$KF_PIPELINES_VERSION" >> /root/.bashrc
echo "export KF_PIPELINES_VERSION=$KF_PIPELINES_VERSION" >> /etc/environment
pip install https://storage.googleapis.com/ml-pipeline/release/${KF_PIPELINES_VERSION}/kfp.tar.gz --upgrade --no-cache --ignore-installed

# Install PipelineAI
sleep 30
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30
kubectl create -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml

pipeline cluster-kube-install --tag $PIPELINE_VERSION --chip=cpu --namespace=kubeflow --image-registry-url=gcr.io/pipelineai2 --users-storage-gb=200Gi --ingress-type=nodeport --users-root-path=/mnt/pipelineai/users

sleep 30
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-istio-noauth.yaml

sleep 30
export ISTIO_VERSION=1.2.2
echo "export ISTIO_VERSION=$ISTIO_VERSION" >> /root/.bashrc
echo "export ISTIO_VERSION=$ISTIO_VERSION" >> /etc/environment
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=${ISTIO_VERSION} sh -
#cd istio-${ISTIO_VERSION}

helm del --purge istio
helm del --purge istio-init
kubectl apply -f /root/pipeline/kubeflow/infrastructure/istio-1.2.2/install/kubernetes/helm/helm-service-account.yaml
helm init --service-account tiller
helm install /root/pipeline/kubeflow/infrastructure/istio-1.2.2/install/kubernetes/helm/istio-init --name istio-init --namespace istio-system
helm install /root/pipeline/kubeflow/infrastructure/istio-1.2.2/install/kubernetes/helm/istio --name istio --namespace istio-system --set gateways.istio-ingressgateway.type=NodePort --set grafana.enabled=true --set kiali.enabled=true --set prometheus.enabled=true --set tracing.enabled=true --set "kiali.dashboard.grafanaURL=http://grafana:3000"

# Istio - Label the namespace
kubectl label namespace istio-system istio-injection=enabled

# Prometheus
kubectl apply -f /root/pipeline/kubeflow/infrastructure/telemetry/conf/prometheus-gateway.yaml
#Grafana
kubectl apply -f /root/pipeline/kubeflow/infrastructure/telemetry/conf/grafana-gateway.yaml
#Kiali
kubectl apply -f /root/pipeline/kubeflow/infrastructure/telemetry/conf/kiali-secret.yaml
kubectl apply -f /root/pipeline/kubeflow/infrastructure/telemetry/conf/kiali-gateway.yaml

kubectl describe svc istio-ingressgateway -n istio-system

# Create kubeflow assets
cd /root 
git clone https://github.com/PipelineAI/pipeline

# Kfctl
export KFAPP=install-kubeflow
echo "export KFAPP=$KFAPP" >> /root/.bashrc
echo "export KFAPP=$KFAPP" >> /etc/environment
cd /root/pipeline/kubeflow/
kfctl init --namespace=kubeflow --use_istio=true ${KFAPP}
cd /root/pipeline/kubeflow/install-kubeflow/
kfctl generate all -V
git checkout /root/pipeline/kubeflow/install-kubeflow/ks_app/components/
git checkout /root/pipeline/kubeflow/install-kubeflow/ks_app/vendor/

sleep 30
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30
kubectl create -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml

cd /root/pipeline/kubeflow/install-kubeflow/
kfctl apply all -V

# Cloud-specific stuff
# Install AWS CLI
pip install awscli
aws ecr get-login --region=us-west-2 --no-include-email | bash

# Install GCP gcloud
CLOUD_SDK_REPO="cloud-sdk-$(grep VERSION_CODENAME /etc/os-release | cut -d '=' -f 2)"
echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
apt-get update && apt-get install -y google-cloud-sdk

# Install APIs for TPUs (Optional)
pip install --upgrade google-api-python-client
pip install --upgrade oauth2client

# Create user-gcp-sa secret
cd /root
wget https://s3.amazonaws.com/fluxcapacitor.com/kubeflow-workshop/user-gcp-sa-secret-key.json
kubectl create secret generic --namespace=kubeflow user-gcp-sa --from-file=user-gcp-sa.json=/root/user-gcp-sa-secret-key.json

# TODO:  Figre out if this is still needed
#kubectl create secret generic docker-registry-secret --from-file=.dockerconfigjson=/root/.docker/config.json --type=kubernetes.io/dockerconfigjson

# Nginx
apt-get install -y nginx
rm /etc/nginx/sites-available/default
rm /etc/nginx/sites-enabled/default
cd /etc/nginx/sites-available/ && ln -s /root/pipeline/kubeflow/infrastructure/config/nginx/pipelineai-nginx.conf
cd /etc/nginx/sites-enabled/ && ln -s /etc/nginx/sites-available/pipelineai-nginx.conf
cd /root
# Restart for Good Measure
service nginx start
service nginx restart

sleep 30
kubectl delete -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml
sleep 30
kubectl create -f /root/.pipelineai/cluster/yaml/.generated-openebs-storageclass.yaml

# Install update TFJob CRD (tfjobs.kubeflow.org)
kubectl delete -f /root/pipeline/kubeflow/infrastructure/crd/tfjob-crd-v1.yaml
sleep 5
kubectl create -f /root/pipeline/kubeflow/infrastructure/crd/tfjob-crd-v1.yaml

# Helm
cd /root
wget https://get.helm.sh/helm-v2.14.1-linux-amd64.tar.gz
tar -xvzf helm-v2.14.1-linux-amd64.tar.gz
chmod a+x linux-amd64/helm
mv linux-amd64/helm /usr/bin/
helm init
sleep 30

# Patch Tiller RBAC per https://github.com/kubeflow/tf-operator/issues/106
kubectl create serviceaccount --namespace kube-system tiller
kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'
sleep 30
helm init --service-account tiller --upgrade

# Seldon
# kubectl create clusterrolebinding seldon-admin --clusterrole=cluster-admin --serviceaccount=${NAMESPACE}:default

sleep 30
# Ambassador (Deprecated)
#helm install seldon-core-operator --namespace kubeflow --set ambassador.enabled=true --repo https://storage.googleapis.com/seldon-charts
# Istio (Migrating to this)
helm install seldon-core-operator --namespace kubeflow --set istio.enabled=true --set ambassador.enabled=true --repo https://storage.googleapis.com/seldon-charts
kubectl create -f /root/pipeline/kubeflow/notebooks/deployments/deployment-gateway.yaml
#helm install seldon-core-analytics --namespace kubeflow --repo https://storage.googleapis.com/seldon-charts 

sleep 30
kubectl apply -f /root/pipeline/kubeflow/infrastructure/rbac/jupyter-notebook-role.yaml

# Pach
# http://<server-ip>:30080
# https://github.com/dwhitena/oreilly-ai-k8s-tutorial/blob/master/README.md
# https://pachyderm.readthedocs.io/en/latest/getting_started/beginner_tutorial.html
curl -o /tmp/pachctl.deb -L https://github.com/pachyderm/pachyderm/releases/download/v1.9.0/pachctl_1.9.0_amd64.deb && sudo dpkg -i /tmp/pachctl.deb

pachctl deploy local --namespace kubeflow
pachctl create-repo raw_data
pachctl list-repo
pachctl put-file raw_data master github_issues_medium.csv -f https://nyc3.digitaloceanspaces.com/workshop-data/github_issues_medium.csv
pachctl list-repo

kubectl create secret generic dockersecret --from-file=.dockerconfigjson=/root/.docker/config.json --type=kubernetes.io/dockerconfigjson

# Spark
# Patch Spark RBAC per https://apache-spark-on-k8s.github.io/userdocs/running-on-kubernetes.html#configuring-kubernetes-roles-and-service-accounts
# --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark # DO WE NEED NAMESPACE, TOO
kubectl create serviceaccount --namespace kubeflow spark
kubectl create clusterrolebinding spark-role --clusterrole=edit --serviceaccount=kubeflow:spark --namespace=kubeflow

kubectl get serviceaccount
kubectl get namespace
kubectl get storageclass
kubectl get pods --all-namespaces
kubectl get svc --all-namespaces
kubectl get deploy --all-namespaces
kubectl get pvc --all-namespaces
kubectl get daemonset --all-namespaces
kubectl get configmap --all-namespaces
kubectl get secrets --all-namespaces
kubectl get gateway --all-namespaces
kubectl get virtualservice --all-namespaces
kubectl get crd --all-namespaces

# Copy data to airflow
apt-get install -y jq
users_pvc_dir=$(kubectl get pvc users-pvc -o json | jq .spec.volumeName | sed -e 's/^"//' -e 's/"$//')
users_pvc_dir=/mnt/pipelineai/users/${users_pvc_dir}
echo ${users_pvc_dir}
ls -al ${users_pvc_dir}
cp -R /root/pipeline/kubeflow/airflow-dags ${users_pvc_dir}
cp -R /root/pipeline/kubeflow/kubeflow-pipelines ${users_pvc_dir}
ls -al ${users_pvc_dir}

# Create community notebook
curl -d "nm=community&ns=kubeflow&imageType=standard&standardImages=pipelineai%2Fkubeflow-notebook-cpu-1.13.1%3A2.0.0&customImage=&cpu=2.0&memory=12.0Gi&ws_type=Existing&ws_name=community&ws_mount_path=%2Fhome%2Fjovyan&vol_type1=Existing&vol_name1=users-pvc&vol_mount_path1=%2Fmnt%2Fpipelineai%2Fusers&extraResources=%7B%7D" -H "Content-Type: application/x-www-form-urlencoded" -X POST http://localhost/jupyter/api/namespaces/kubeflow/notebooks

sleep 30

# Copy notebooks to community pvc
community_pvc_dir=$(kubectl get pvc community -o json | jq .spec.volumeName | sed -e 's/^"//' -e 's/"$//')
community_pvc_dir=/mnt/pipelineai/users/${community_pvc_dir}
echo ${community_pvc_dir}
ls -al ${community_pvc_dir}
cp -R /root/pipeline/kubeflow/notebooks ${community_pvc_dir}
ls -al ${community_pvc_dir}

# Create.orig
#export KFAPP=install-kubeflow
#echo "export KFAPP=$KFAPP" >> /root/.bashrc
#echo "export KFAPP=$KFAPP" >> /etc/environment
# Default uses IAP.
#kfctl init --namespace=kubeflow --use_istio=true ${KFAPP}
#cd ${KFAPP}
#kfctl generate all -V
#kfctl apply all -V

# Delete
#cd /root/pipeline/kubeflow/install-kubeflow/ks_app
#ks delete default

# THIS MIGHT CAUSE THE kubeflow NAMESPACE TO HANG DURING TERMINATION
# DELETE ENTIRE NAMESPACE INCLUDING PVC's!!!
#cd ${KFAPP}
# This also deletes PVC's
#kfctl delete all
#kfctl delete all --delete_storage
