## Step 1:  [Setup Docker and Kubernetes CLI](setup.md)

**Note:  All commands must be run within the Docker container started in Step 1!!**

## Step 2:  Create Kubernetes Cluster on GCP
* Update `NUM_NODES`, `MACHINE_TYPE`, and autoscale config params accordingly
```
export KUBERNETES_PROVIDER=gke
export CLUSTER_NAME=gkecluster
export NUM_NODES=1
# This must be us-east1-d to support Kubernetes 1.6 (as of 2017/04/05)
export ZONE=us-east1-d
export MACHINE_TYPE=n1-highmem-8
export KUBE_ENABLE_CLUSTER_AUTOSCALER=true
export KUBE_AUTOSCALER_MIN_NODES=1
export KUBE_AUTOSCALER_MAX_NODES=1
export KUBE_TARGET_NODE_UTILIZATION=0.75
export GKE_CREATE_FLAGS=--disk-size=200 --node-labels=training=true,prediction=true
export CLUSTER_API_VERSION=1.6.0

# Additional (Optional)
export ADDITIONAL_ZONES=
export HEAPSTER_MACHINE_TYPE=
export CLUSTER_IP_RANGE=
export NODE_SCOPES=
```
More details [here](https://cloud.google.com/sdk/gcloud/reference/container/clusters/create).

### Init `gcloud` CLI
```
gcloud init
```

### Login to GCP
```
gcloud auth login

### EXPECTED OUTPUT ###
Go to the following link in your browser:

    https://accounts.google.com/o/oauth2/auth?redirect_uri=...

Enter verification code: <-- Type Verification Code

### EXPECTED OUTPUT ###
WARNING: `gcloud auth login` no longer writes application default credentials.
If you need to use ADC, see:
  gcloud auth application-default --help

You are now logged in as [<user-name>].
Your current project is [default].  You can change this setting by running:
  $ gcloud config set project PROJECT_ID
```

### Setup Application Default Credentials (ADC)
```
gcloud auth application-default login

### EXPECTED OUTPUT ###
Go to the following link in your browser:

    https://accounts.google.com/o/oauth2/auth?redirect_uri=...

Enter verification code: ...

Credentials saved to file: [/root/.config/gcloud/application_default_credentials.json]
```

### Setup Project Name within GCP
* YOU MUST SET THIS EVEN IF `default` IS THE CORRECT NAME!!!
```
gcloud config set project <PROJECT-ID>
```

### Setup GCP Application-Default Credentials
```
gcloud auth application-default login
```

### Start Kubernetes Cluster and Wait 3-5 Mins
```
$KUBERNETES_HOME/cluster/kube-up.sh

### EXPECTED OUTPUT ###
... Starting cluster in <zone> using provider gke
... calling verify-prereqs
... in gke:verify-prereqs()
... calling verify-kube-binaries
... calling kube-up
... in gke:kube-up()
... in gke:detect-project()
... Using project: flux-capacitor1
... Using network: default
... Using firewall-rule: default-allow-ssh
...
...
... Using project: <project-name>
... Using network: default
... Using firewall-rule: default-allow-ssh
Creating cluster [cluster_name]...done.
Created [https://container.googleapis.com/v1/projects/flux-capacitor1/zones/us-east1-d/clusters/[cluster_name]].
kubeconfig entry generated for [cluster_name].
NAME           ZONE        MASTER_VERSION  MASTER_IP  MACHINE_TYPE   NODE_VERSION   NUM_NODES  STATUS
[cluster_name] [zone]      [kube_version]  [ip]       n1-highmem-16  [kube_version] 1          RUNNING
... calling validate-cluster
```

### Setup GKE Cluster Credentials 
```
gcloud container clusters get-credentials --zone [zone] [clustername]
```

### Disable TLS x509 Validation (For Now)
```
kubectl config view

### EXPECTED OUTPUT ###
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: REDACTED
    server: https://...
  name: gke_project-name_zone_clustername  <-- COPY THIS CLUSTER NAME
contexts:
- context:
    cluster: gke_project-name_zone_clustername
    user: gke_project-name_zone_clustername
  name: gke_project-name_zone_clustername
current-context: gke_project-name_zone_clustername
kind: Config
preferences: {}
users:
- name: gke_project-name_zone_clustername
  user:
    auth-provider:
      config:
        access-token: ...
        expiry: ...
      name: gcp
```

```
kubectl config set-cluster <cluster-name-from-above> --insecure-skip-tls-verify=true

### EXPECTED OUTPUT ###
Cluster "<cluster-name-from-above>" set.
```

## Step 3:  [Setup PipelineAI on Kubernetes](pipelineai.md)

## Reference
### Kubernetes Dashboard
Get Cluster Credentials
```
kubectl config view

### EXPECTED OUTPUT ###
# apiVersion: v1
# clusters:
# - cluster:
#     certificate-authority-data: REDACTED
#     server: https://<your-cluster-name>
#   name: <your-cluster-name>
#
# ...
#
# users:
# - name: <your-cluster-name>-basic-auth
# user:
#   password: ...   <-- COPY THIS PASSWORD!
#   username: admin                        
```
**COPY ^^^THIS^^^ PASSWORD!**
``` 
kubectl cluster-info

### EXPECTED OUTPUT ###
...
Kubernetes-dashboard is running at ...  <-- COPY THIS URL
```
Navigate to the following:

**Note:  Username `admin`, Password is from above (or found in the GKE Web UI)**
```
https://<url-from-previous-step>/api/v1/proxy/namespaces/kube-system/services/kubernetes-dashboard/#/workload?namespace=default
```

## Step 3: Train and Serve ML/AI Models with PipelineAI
Follow [THESE](https://github.com/PipelineAI/pipeline/) instructions to train and serve models with PipelineAI.

## Step 4: (Optional) Delete the Cluster
```
$KUBERNETES_HOME/cluster/kube-down.sh
```
