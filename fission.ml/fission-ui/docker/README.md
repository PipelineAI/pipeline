# Fission-ui Deployment using docker

Fission-ui is a single page application, so it only contains static files and depends on fission APIs 
on path `/proxy/{service}`.

## Deploy with other fission components in k8s cluster

The easiest way is to deploy fission-ui as an micro-service with other fission services in fission namespace. 

After deploying fission services, create a fission-ui service and deployment:
```
$ kubectl create -f fission-ui.yaml
```
You can then access the ui via `node_ip:31319` or load balance.

We use Nginx to serve the static files, which are complied by `npm run build` and piped into the project build folder. 
It also handles api requests to the fission services inside k8s. 

## Deploy a standalone fission-ui server

Modify the Nginx config `fission-ui.conf`, change the fission service name to your fission service ip or domain name.

## Other features
You can add other features like https, authentication by modifying Nginx config file.
