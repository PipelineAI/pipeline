# Fission UI [![Build Status](https://travis-ci.org/fission/fission-ui.svg?branch=master)](https://travis-ci.org/fission/fission-ui)

Fission-ui is a web-based UI for [fission](https://github.com/fission/fission).
It allows users to observe and manage fission, 
providing a simple online development environment for serverless functions powered by fission.

## Features
- The UI service can be deployed with fission in k8s easily
- A dashboard overview of all functions and triggers related
- Test and see the response of the function right from the editor
- A draft mode let you test the function in your editor without affecting the online version
- Multi-language support

## Running fission-ui

### Deploy Fission-ui with other fission services
After deploying fission services, create a fission-ui service and deployment:
```
$ kubectl create -f docker/fission-ui.yaml
```
You can then access the ui via `node_ip:31319` or the load balancer.

In `fission-ui.yaml`, we simply define a deployment and a service map node port 31319 to 80
port of the pods labeled `srv:fission-ui`. The pod of the deployment is a Nginx which hosts the
static files of the ui and proxy `/proxy` to fission controller/router. The deployment can be
scaled horizontally.

### Manage fission environments
- Create environments by providing name, docker image or by selecting a sample
- Modify docker image of a environment
- Delete a specified environment (currently no check on the functions depends on it)

### Observe function status
The function list will explain the name, env, triggers of each function. You can jump to edit the
env, triggers of the function easily. When you delete a function with triggers related, fission-ui
will ask if you want to delete the related triggers, and those triggers will be deleted with the
function by default.

### Create a new function
By clicking on the `Add` button of the function list, you need to provide the name, env and code.
The function is not created until you click on `Deploy` button and no error occurs.
After that, you will be redirected to the edit page of the function.

### Upload functions in batch
By clicking on the `Batch Upload` button, you can create/update multiple functions from local files.

- You need to create a mapping from file extension to fission environment.
This helps fission-ui to extract name, environment and code from selected local files
- Choose or drag & drop the function files you want to upload
- Select the upload mode to create or update. If you attempt to create an existed function, fission will return
an error
- Click on `Upload` and the functions will be uploaded one by one.

### Edit a function
The name and environment of a created function cannot be modified. So only the code of a function can be updated.

### Deploy a function
By clicking on the `Deploy` button, you are saving the code in your online editor to fission. 
If you invoke the function, the latest version of the function will be loaded after few seconds.

### Test a function through http trigger
We can only test a function manually through http triggers. Other events are not manually invokable now.
There are two modes: the main and draft mode. 

The draft mode is the default. Once you click on `Test`, the code in your editor will be used to an temp function,
and the http request you submit will be passed to the temp function once it is ready (in about 3 seconds according to
fission sync interval). Once fission-ui receives it's response, the temp function will be deleted. So there are no env 
reuse for the draft mode. It means every time a draft test is fired, you have to wait for the function creation,
env specialization, request and response, function deletion. But the draft function will not affect the function and all
the traffic to the original function is safe.

The main mode is easier. It simply passes you request to the latest version of the function and get the response. It is 
fast because of env reuse.

### Manage all kinds of triggers related to functions

Functions in fission are event-driven. A function is invoked if a related trigger emits event.
You can manage the triggers related to a function in the trigger tab.
Currently http triggers and kube watchers only. Timer, custom-application trigger is expected.

#### Http Triggers
In fission, a function is given a http trigger by default, which is `/fission-function/${function-name}`.
It invokes the latest version of the function.
Fission-ui uses the default for testing. Defining a http trigger by yourself is better by providing path(url pattern)
and the http method.

#### Kube Watchers
By providing namespace, obj type and label selector, you can create a kube watcher trigger for a function.

### More features to come

Fission-ui will update as fission evolves.
- Observe and manage function logs, versions and statics
- Control function runtime resources
- Fission environment v2
- Testing function through mocked all kinds of events
- Better interface

## Development

Fission-ui is created from [react-boilerplate](https://github.com/react-boilerplate/react-boilerplate).
It is a single page application and uses react, redux, react-saga.
Fission-ui is under rapid development like fission, so feel free to get the code and play with it.

1. Clone this repo using `git clone https://github.com/fission/fission-ui`
1. Run `yarn install` or `npm install` to install dependencies
1. `export FISSION_ROUTER=$SERVER_IP:31313` and `export FISSION_CONTROLLER=$SERVER_IP:31314`
1. Run `npm run start` to see the app at `http://localhost:3000`
1. Run `npm run build` to build the application into the build folder
1. Run `cd docker && ./push.sh $DOCKER_HUB_NAME $TAG` to build docker image and push to docker hub

## License

The work done has been licensed under Apache License 2.0.

## Status
Fission UI is in early alpha. It's not suitable for production use just yet.
