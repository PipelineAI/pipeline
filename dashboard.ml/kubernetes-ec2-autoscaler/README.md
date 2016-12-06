# Based on External Repo 
* https://github.com/openai/kubernetes-ec2-autoscaler

# kubernetes-ec2-autoscaler

kubernetes-ec2-autoscaler is a node-level autoscaler for [Kubernetes](http://kubernetes.io/)
on AWS EC2 that is designed for batch jobs. Kubernetes is a container orchestration framework
that schedules Docker containers on a cluster, and kubernetes-ec2-autoscaler can scale
[AWS Auto Scaling Groups](http://docs.aws.amazon.com/autoscaling/latest/userguide/WhatIsAutoScaling.html)
based on the pending job queue.

The key features are:
- Scaling on flexible resource requirements: the autoscaler determines the resources it needs
from the pending job queue, and scales up the appropriate ASGs while respecting job
contraints such as node selectors
- Multi-Region Support: the autoscaler can detect scaling errors and overflow to secondary
AWS regions
- Draining nodes on scale in: the autoscaler makes sure to not kill in-flight jobs

## Setup

The autoscaler can be run anywhere as long as it can access the AWS
and Kubernetes APIs, but the recommended way is to set it up as a
Kubernetes Pod.

### Auto Scaling Groups
Your Auto Scaling Groups must be tagged with `KubernetesCluster`, whose value should be passed
to the flag `--cluster-name`, and `Role` with a value of `minion`. If you use `kube-up` to set
up your cluster, those will be set automatically.

You must also enable [Instance Protection](http://docs.aws.amazon.com/autoscaling/latest/userguide/as-instance-termination.html#instance-protection) on all your Auto Scaling Groups, since instance
termination will be handled by the kubernetes-ec2-autoscaler.

### IAM User
It is highly recommended that you make an [AWS IAM user](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_users.html)
for this service, instead of using your worker's instance profile,
since it will need permissions to terminate instances.
Here is the minimal IAM policy required by the autoscaler:
```
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "autoscaling:SetDesiredCapacity",
        "autoscaling:TerminateInstanceInAutoScalingGroup",
        "autoscaling:DescribeAutoScalingGroups",
        "autoscaling:DescribeScalingActivities",
        "autoscaling:DescribeLaunchConfigurations"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
```

### Credentials
Once you have an IAM user, you will need its [access key](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html).
The best way to use the access key in Kubernetes is with a [secret](http://kubernetes.io/docs/user-guide/secrets/).
Here is a sample format for `secret.yaml`:
```
apiVersion: v1
kind: Secret
metadata:
  name: autoscaler
  namespace: system
data:
  aws-access-key-id: [base64 encoded access key]
  aws-secret-access-key: [base64 encoded secret access key]
  slack-hook: [optional slack incoming webhook]
```
You can then save it in Kubernetes:
```
$ kubectl create -f secret.yaml
```

### Run
[scaling-controller.yaml](scaling-controller.yaml) has an example
[Replication Controller](http://kubernetes.io/docs/user-guide/replication-controller/)
that will set up Kubernetes to always run exactly one copy of the autoscaler.
To create the Replication Controller:
```
$ kubectl create -f scaling-controller.yaml
```
You should then be able to inspect the pod's status and logs:
```
$ kubectl get pods --namespace system -l app=autoscaler
NAME               READY     STATUS    RESTARTS   AGE
autoscaler-opnax   1/1       Running   0          3s

$ kubectl logs autoscaler-opnax --namespace system
2016-08-25 20:36:45,985 - autoscaler.cluster - DEBUG - Using kube service account
2016-08-25 20:36:45,987 - autoscaler.cluster - INFO - ++++++++++++++ Running Scaling Loop ++++++++++++++++
2016-08-25 20:37:04,221 - autoscaler.cluster - INFO - ++++++++++++++ Scaling Up Begins ++++++++++++++++
2016-08-25 20:37:04,255 - autoscaler.autoscaling_groups - DEBUG - c4.2xlarge kubernetes-worker - 0 has no timeout
...
```

## Configuration

```
$ python main.py [options]
```

- --cluster-name: Name of the Kubernetes cluster. Must match the value of the `KubernetesCluster` tag on Auto Scaling Groups.
- --regions: List of comma-separated regions in order of preference. E.g. `us-west-2,us-east-1` will use "us-west-2" as the
primary region and "us-east-1" as the secondary.
- --kubeconfig: Path to kubeconfig YAML file. Leave blank if runnning in Kubernetes to use [service account](http://kubernetes.io/docs/user-guide/service-accounts/).
- --idle-threshold: This defines the maximum duration (in seconds) for an instance to be kept idle.
- --type-idle-threshold: For each instance type, we keep a few running and idle so the cluster has spare capacity for different types of requests. This defines the maximum duration (in seconds) for an instance to be kept idle.
- --aws-access-key: AWS access key. Can also be specified in environment variable `AWS_ACCESS_KEY`
- --aws-secret-key: AWS secret access key. Can also be specified in environment variable `AWS_SECRET_ACCESS_KEY`
- --instance-init-time: Maximum duration (in seconds) after an instance is launched before being considered unhealthy (running in EC2 but not joining the Kubernetes cluster)
- --sleep: Time (in seconds) to sleep between scaling loops (to be careful not to run into AWS API limits)
- --slack-hook: Optional [Slack incoming webhook](https://api.slack.com/incoming-webhooks) for scaling notifications
- --dry-run: Flag for testing so resources aren't actually modified. Actions will instead be logged only.
- -v: Sets the verbosity. Specify multiple times for more log output, e.g. `-vvv`

## Developing

### Locally
```
# in your virtual env
$ pip install -r requirements.txt
$ python main.py --regions us-west-2,us-east-1,us-west-1 --cluster-name my-kubernetes --aws-access-key 'XXXXXXX' --aws-secret-key 'XXXXXXXXXXXXX' --dry-run -vvv --kubeconfig ~/.kube/config
$ nosetests test/
```

### Docker
```
$ docker build -t autoscaler .
$ docker run -v $HOME/.kube/config:/root/.kube/config autoscaler python main.py --regions us-west-2,us-east-1,us-west-1 --cluster-name my-kubernetes --aws-access-key 'XXXXXXXXX' --aws-secret-key 'XXXXXXXXXXXXX' --dry-run -vvv --kubeconfig /root/.kube/config
$ docker run -v $HOME/.kube/config:/root/.kube/config autoscaler nosetests test/
```
