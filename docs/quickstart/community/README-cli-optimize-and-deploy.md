Work In Progress - Not Yet Supported

## CLI - resource_optimize_and_deploy
You can copy the `resource_optimize_and_deploy` cli command from the Example section of the `resource-upload` command output.  The command is automatically injected with your parameter values.
 
You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see the UserId in the upper right hand corner of the Settings tab after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)

![user-id](https://pipeline.ai/assets/img/user-id-2.png)

* `<YOUR_MODEL_NAME>` - User defined model name that uniquely identifies the model
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the model version
* `<YOUR_RESOURCE_ID>` - Id that uniquely identifies the uploaded model, resource-id is generated and returned by the `resource-upload` command
```
pipeline resource-optimize-and-deploy --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --name <YOUR_MODEL_NAME> --tag <YOUR_TAG_NAME> --resource-subtype tensorflow --runtime-list tfserving --chip-list cpu --resource-id <YOUR_RESOURCE_ID>
```

# Predict with CLI
You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see the UserId in the upper right hand corner of the Settings tab after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)</br>
![user-id](https://pipeline.ai/assets/img/user-id.png)
* `<YOUR_MODEL_NAME>` - User defined model name that uniquely identifies the model
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the model version

```
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR_USER_ID>/<YOUR_MODEL_NAME>/invoke --test-request-path=./tensorflow/mnist-v3/model/pipeline_test_request.json --test-request-concurrency=1

### EXPECTED OUTPUT ###
...

{"variant": "mnist-<YOUR-MODEL-ID>-tensorflow-tfserving-cpu", "outputs":{"classes": [2], "probabilities": [[0.00681829359382391, 1.2452805009388612e-08, 0.5997999310493469, 0.2784779667854309, 6.614273297600448e-05, 0.10974341630935669, 0.00015215273015201092, 0.0002482662384863943, 0.004691515117883682, 2.2582455585506978e-06]]}}
...

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.00138249155133962
1      0.00036483019357547
2      0.59979993104934693  <-- Prediction
3      0.01074937824159860
4      0.00158193788956850
5      0.00006451825902331
6      0.00010775036207633
7      0.00010466964886290
8      0.04691515117883682   
9      0.00000471303883387
```

## Perform 100 Predictions in Parallel (Mini Load Test)
```
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR_USER_ID>/mnist/invoke --test-request-path=./tensorflow/mnist-v3/model/pipeline_test_request.json --test-request-concurrency=100
```
