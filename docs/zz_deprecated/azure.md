## Step 1:  [Setup Docker and Kubernetes CLI](setup.md)

## Step 2:  Setup Kubernetes 

**Note:  All commands must be run within the Docker container started in Step 1!!**

### Setup Environment Variables
```
export RESOURCE_GROUP=pipeline-azuredemo
export LOCATION=westus
export DNS_PREFIX=<unique-pipeline-azuredemo-prefix-such-as-your-username>
export SERVICE_NAME=pipeline-service
```

### Login With Azure CLI
```
az login

### EXPECTED OUTPUT ###
To sign in, use a web browser to open the page https://aka.ms/devicelogin and enter the code REDACTED to authenticate.
[
  {
    "cloudName": "AzureCloud",
    "id": "f89b3492-3c3f-4365-805c-b43e85c3e77e",
    "isDefault": true,
    "name": "fluxcapacitor",
    "state": "Enabled",
    "tenantId": "fb11b146-c09f-4d62-928e-54540a54127b",
    "user": {
      "name": "chris@fregly.com",
      "type": "user"
    }
  }
]
```

### Create Resource Group
```
az group create --name=$RESOURCE_GROUP --location=$LOCATION

### EXPECTED OUTPUT ###
{
  "id": "/subscriptions/f89b3492-3c3f-4365-805c-b43e85c3e77e/resourceGroups/pipeline-azuredemo",
  "location": "westus",
  "managedBy": null,
  "name": "pipeline-azuredemo",
  "properties": {
    "provisioningState": "Succeeded"
  },
  "tags": null
}
```

### Create Cluster
```
az acs create --generate-ssh-keys --orchestrator-type=kubernetes --resource-group $RESOURCE_GROUP --name=$SERVICE_NAME --dns-prefix=$DNS_PREFIX

### EXPECTED OUTPUT ###
{
  "id": "/subscriptions/f89b3492-3c3f-4365-805c-b43e85c3e77e/resourceGroups/pipeline-azuredemo/providers/Microsoft.Resources/deployments/azurecli1485661870.2932633",
  "name": "azurecli1485661870.2932633",
  "properties": {
    "correlationId": "26a58327-c7ce-4771-a15b-fb6723a9ada5",
    "debugSetting": null,
    "dependencies": [],
    "mode": "Incremental",
    "outputs": null,
    "parameters": {
      "clientSecret": {
        "type": "SecureString"
      }
    },
    "parametersLink": null,
    "providers": [
      {
        "id": null,
        "namespace": "Microsoft.ContainerService",
        "registrationState": null,
        "resourceTypes": [
          {
            "aliases": null,
            "apiVersions": null,
            "locations": [
              "westus"
            ],
            "properties": null,
            "resourceType": "containerServices"
          }
        ]
      }
    ],
    "provisioningState": "Succeeded",
    "template": null,
    "templateLink": null,
    "timestamp": "2017-01-29T04:12:47.109982+00:00"
  },
  "resourceGroup": "pipeline-azuredemo"
}
```

### Verify Azure CLI Configuration
```
az acs show -g $RESOURCE_GROUP -n $SERVICE_NAME --output list

### EXPECTED OUTPUT ###
Custom Profile            : None
Id                        : /subscriptions/f89b3492-3c3f-4365-805c-b43e85c3e77e/resourceGroups/pipeline-azuredemo/providers/Microsoft.ContainerService/containerServices/pipeline-service
Location                  : westus
Name                      : pipeline-service
Provisioning State        : Succeeded
Resource Group            : pipeline-azuredemo
Tags                      : None
Type                      : Microsoft.ContainerService/ContainerServices
Windows Profile           : None
Agent Pool Profiles       :
   Count      : 3
   Dns Prefix : pipeline-azuredemo-k8s-agents
   Fqdn       :
   Name       : agentpools
   Vm Size    : Standard_D2_v2
Diagnostics Profile       :
   Vm Diagnostics :
      Enabled     : True
      Storage Uri : None
Linux Profile             :
   Admin Username : azureuser
   Ssh            :
      Public Keys :
         Key Data : ssh-rsa REDACTED chris@fregly.com

Master Profile            :
   Count      : 1
   Dns Prefix : pipeline-azuredemo
   Fqdn       : pipeline-azuredemo.westus.cloudapp.azure.com    <-- COPY THIS 
Orchestrator Profile      :
   Orchestrator Type : Kubernetes
Service Principal Profile :
   Client Id : 419ba3ca-cad9-4b3d-9395-d6d906b89610
   Secret    : None
```

### Download Azure `~/.kube/config`
```
scp azureuser@<cluster-fqdn-from-above>:.kube/config ~/.kube/azure-config
```

### Merge Azure `~/.kube/azure-config` and `~/.kube/config`

### Verify `kubectl` CLI Configuration
```
kubectl config view

### EXPECTED OUTPUT ###
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: REDACTED
    server: https://pipeline-azuredemo.westus.cloudapp.azure.com
  name: pipeline-azuredemo  <-- COPY THIS CLUSTER_NAME
contexts:
- context:
    cluster: pipeline-azuredemo
    user: pipeline-azuredemo-admin
  name: azuredemo
current-context: azuredemo
kind: Config
preferences: {}
users:
- name: pipeline-azuredemo-admin
  user:
    client-certificate-data: REDACTED
    client-key-data: REDACTED
```

### Get Cluster Credentials
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
* **COPY ^^^THIS^^^ PASSWORD!**

### Verify Kubernetes Dashboard
``` 
kubectl cluster-info

### EXPECTED OUTPUT ###
...
Kubernetes-dashboard is running at ...  <-- COPY THIS URL
```
### Navigate to the following:

**Note:  Username and Password are in the Azure Container Service Web UI**

```
https://<url-from-previous-step>/api/v1/proxy/namespaces/kube-system/services/kubernetes-dashboard/#/workload?namespace=default
```

### Disable TLS Auth (For Now)
```
kubectl config set-cluster <cluster-name-from-above> --insecure-skip-tls-verify=true

### EXPECTED OUTPUT ###
Cluster "<cluster-name-from-above>" set.
```


## Step 3: Train and Serve ML/AI Models with PipelineAI
Follow [THESE](https://github.com/PipelineAI/pipeline/) instructions to train and serve models with PipelineAI.

## Step 4: (Optional) Delete the Cluster
* Make sure these Environment Variables have been set up above
```
kops delete --state ${KOPS_STATE_STORE} cluster --name ${CLUSTER_NAME}
```
* Add `--yes` to the command above when you're ready to delete the cluster
