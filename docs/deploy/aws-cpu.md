## Step 1: Setup Pre-requisites
* [Download and Install](setup.md) the pre-built PipelineAI Installation Docker Image

-- OR -- 

* Install [AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
* Install [Docker Community Edition](https://www.docker.com/community-edition#/download)
* Install [Miniconda](https://conda.io/docs/install/quick.html) with Python 3 Support
* Install [Kubernetes CLI](https://kubernetes.io/docs/tasks/tools/install-kubectl/) 
* Install [KOPS](https://github.com/kubernetes/kops#installing)

## Step 2:  Create Kubernetes Cluster on AWS

**Note:  ALL COMMANDS MUST BE RUN WITHIN THE DOCKER CONTAINER STARTED IN STEP 1!!**

**We cannot support one-off environments!**

### Setup AWS CLI
```
aws configure
```
Enter `ACCESS_KEY_ID`, `SECRET_ACCESS_KEY`, and Default Region (ie. `us-west-2`)

## Create IAM Role for `kops`
```
aws iam create-group --group-name kops

aws iam attach-group-policy --policy-arn arn:aws:iam::aws:policy/AmazonEC2FullAccess --group-name kops
aws iam attach-group-policy --policy-arn arn:aws:iam::aws:policy/AmazonRoute53FullAccess --group-name kops
aws iam attach-group-policy --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess --group-name kops
aws iam attach-group-policy --policy-arn arn:aws:iam::aws:policy/IAMFullAccess --group-name kops
aws iam attach-group-policy --policy-arn arn:aws:iam::aws:policy/AmazonVPCFullAccess --group-name kops

aws iam create-user --user-name kops

aws iam add-user-to-group --user-name kops --group-name kops

aws iam create-access-key --user-name kops
```
**^^ COPY THESE CREDENTIALS SOMEWHERE.  YOU WILL NEED THEM LATER! ^^**

### (Re-)Setup AWS CLI with `kops` IAM Role
```
aws configure
```
Enter new `ACCESS_KEY_ID`, `SECRET_ACCESS_KEY`, and Default Region (ie. `us-west-2`) from above.

### Setup Environment Variables
**AWS Environment Variables**

Because `aws configure` doesn't export these vars for kops to use, we export them below.
```
export AWS_ACCESS_KEY_ID=`aws configure get aws_access_key_id`
export AWS_SECRET_ACCESS_KEY=`aws configure get aws_secret_access_key`
```

**Cluster Name**

__If you see the following, `CLUSTER_NAME` must be a fully-qualified domain name (ie. Route 53) per this [link](https://github.com/kubernetes/kops/blob/master/docs/aws.md#configure-dns).__
```
export CLUSTER_NAME=<your-cluster-name-with-fully-qualified-DNS-name>
```
__Cluster Name must be a fully-qualified DNS name (ie. `awscpu.pipeline.ai`)__

**S3 Bucket**

__This bucket must be accessible with the AWS Credentials used in the `aws configure` command above.__
```
export KOPS_STATE_STORE=<your-globally-unique-s3-bucket-name>
```
__State Store must be fully-qualified s3 bucket such as `s3://awscpu.pipeline.ai`__

### Create S3 Bucket for Cluster Definitions
```
aws s3 mb ${KOPS_STATE_STORE}

### EXPECTED OUTPUT ###
# make_bucket: <your-globally-unique-s3-bucket-name>
```

### Create SSH Key Pair
```
ssh-keygen -t rsa

### EXPECTED OUTPUT ###
# Generating public/private rsa key pair.
# Enter file in which to save the key (/root/.ssh/id_rsa):
# Created directory '/root/.ssh'.
# Enter passphrase (empty for no passphrase):
# Enter same passphrase again:
# Your identification has been saved in /root/.ssh/id_rsa.
# Your public key has been saved in /root/.ssh/id_rsa.pub.
# The key fingerprint is:
# ... 
# The key's randomart image is:
# ...
```
**^^ Copy these keys somewhere.  You will need them later! ^^**

### Create Cluster Configuration
```
kops create cluster \
    --cloud aws \
    --dns public \
    --ssh-public-key ~/.ssh/id_rsa.pub \
    --master-zones us-west-2b \
    --master-size t2.medium \
    --zones us-west-2b \
    --node-count 1 \
    --node-size r3.2xlarge \
    --node-tenancy default \
    --kubernetes-version 1.8.4 \
    --image kope.io/k8s-1.7-debian-jessie-amd64-hvm-ebs-2017-07-28 \
    --alsologtostderr \
    --log_dir logs \
    --v 5 \
    --state ${KOPS_STATE_STORE} \
    --name ${CLUSTER_NAME}
```
Note:  You can use `--vpc` to re-use your existing infrastructure.

Note 2:  You can switch to `flannel` for networking by adding `--networking flannel`

Note 3:  Other images available [here](https://github.com/kubernetes/kops/tree/master/channels).

### Edit Cluster Config
```
kops edit cluster --state ${KOPS_STATE_STORE} --name ${CLUSTER_NAME}
``` 

Copy the following at the *BOTTOM* of the `spec:`
```
# FROM HERE
  kubeAPIServer:
    runtimeConfig:
      batch/v2alpha1: "true"
      apps/v1alpha1: "true"      
# TO HERE
```
Alpha feature configuration is described [HERE](https://github.com/kubernetes/kops/blob/master/docs/cluster_spec.md#runtimeconfig)

## Update Instance Groups
### List Current Instance Groups
```
kops get ig --state ${KOPS_STATE_STORE} --name ${CLUSTER_NAME}

### EXPECTED OUTPUT ###
#
Using cluster from kubectl context: <cluster-name>

NAME                ROLE	MACHINETYPE	MIN	MAX	ZONES
master-us-west-2b   Master	t2.medium	1	1	us-west-2b
nodes               Node	r3.2xlarge	1	1	us-west-2b
```

# Sometimes you need to use this
```
kops get ig --state s3://awsgpu.pipeline.ai --name awsgpu.pipeline.ai
```
### Edit `nodes` Instance Group
```
kops edit ig nodes --state ${KOPS_STATE_STORE} --name ${CLUSTER_NAME}
``` 

Copy the following at the *BOTTOM* of the `spec:`
```
# FROM HERE
  rootVolumeSize: 200
  rootVolumeType: gp2
  kubernetesVersion: 1.8.4
# TO HERE
```

### List Current Instance Groups
```
kops get ig --state ${KOPS_STATE_STORE} --name ${CLUSTER_NAME}

### EXPECTED OUTPUT ###
#
NAME                ROLE	MACHINETYPE	MIN	MAX	SUBNETS
master-us-west-2b   Master	t2.medium	1	1	us-west-2b
nodes               Node	r3.2xlarge	1	1	us-west-2b
```

## Update and Create Cluster Config
```
kops update cluster --state ${KOPS_STATE_STORE} --name ${CLUSTER_NAME} --yes
```

** WAIT ABOUT 10-15 MINUTES BEFORE ATTEMPTING THESE NEXT STEPS!! **

## Validate Successful Cluster Startup 
Re-run this until cluster starts successfully.

_If you have issues, either check the logs or the AWS Console (Autoscale Groups, etc)._

```
kops validate cluster
```

### Setup `kubectl` Config
Disable TLS Auth (For Now)
```
kubectl config set-cluster ${CLUSTER_NAME} --insecure-skip-tls-verify=true
```

### Verify Cluster through `kubectl` CLI
```
kubectl get nodes
```

## Step 3: Setup Kubernetes [Add-Ons](https://github.com/kubernetes/kops/blob/master/docs/addons.md)
### Kubernetes [Dashboard](https://github.com/kubernetes/dashboard) <-- HIGHLY RECOMMENDED
```
kubectl create -f https://raw.githubusercontent.com/kubernetes/kops/master/addons/kubernetes-dashboard/v1.7.1.yaml
```

Login to Kubernetes Dashboard
``` 
kubectl cluster-info

### EXPECTED OUTPUT ###
...
Kubernetes-dashboard is running at ...  <-- COPY THIS URL, USE BELOW
```
Navigate your browser to the following:
```
https://<kubernetes-dashboard-url-from-above>
```
* Username: admin
* Password: `kops get secrets kube --type secret -oplaintext --state ${KOPS_STATE_STORE}`

### Heapster (ie. `top` for Kubernetes) <-- HIGHLY RECOMMENDED
Heapster enables the Autoscaling features in Kubernetes
```
kubectl create -f https://raw.githubusercontent.com/kubernetes/kops/master/addons/monitoring-standalone/v1.7.0.yaml
```
### Logging (ElasticSearch) <-- HIGHLY OPTIONAL
```
kubectl create -f https://raw.githubusercontent.com/kubernetes/kops/master/addons/logging-elasticsearch/v1.6.0.yaml
```

## Step 4: Train and Serve ML/AI Models with PipelineAI
Follow [THESE](https://github.com/PipelineAI/pipeline/) instructions to train and serve models with PipelineAI.

## Step 5: (Optional) Delete the Cluster
* Make sure these Environment Variables have been set up above
```
kops delete --state ${KOPS_STATE_STORE} cluster --name ${CLUSTER_NAME}
```
* Add `--yes` to the command above when you're ready to delete the cluster

## More Resources
### More `kops` [Commands](https://github.com/kubernetes/kops/blob/master/docs/instance_groups.md)
* Modify Cluster (`instanceType`, `nodeCount`, `rootVolumeSize`, `rootVolumeOptimization`)
* Use AWS [Spot Instances](https://github.com/kubernetes/kops/blob/master/docs/instance_groups.md#converting-an-instance-group-to-use-spot-instances) (`maxPrice`)

## Troubleshooting
* If you see the following, you need to set the `CLUSTER_NAME` and `KOPS_STATE_STORE` environment variables in your shell before you can run any kops commands.
```
State store "" is not cloud-reachable - please use an S3 bucket
```

* If you see the following error related to `no credentials` or `no space left`, you need to increase the `rootVolumeSize` of your EC2 instance per previous step.
```
Failed to pull image "docker.io/fluxcapacitor/jupyterhub": image pull failed for docker.io/pipelineai/...:<tag>, this may be because there are no credentials on this request. details: (write /mnt/sda1/var/lib/docker/tmp/GetImageBlob871155766: no space left on device) 
Error syncing pod, skipping: failed to "StartContainer" for "jupyterhub" with ErrImagePull: "image pull failed for docker.io/pipelineai/...:<tag>, this may be because there are no credentials on this request. details: (write /mnt/sda1/var/lib/docker/tmp/GetImageBlob871155766: no space left on device)"
```
