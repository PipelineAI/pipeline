## CLI - resource_optimize_and_train
You can copy the `resource_optimize_and_deploy` cli command from the Example section of the `resource-upload` command output.  The command is automatically injected with your parameter values.
 
You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see the UserId in the upper right hand corner of the Settings tab after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)

![user-id](https://pipeline.ai/assets/img/user-id-2.png)

* `<YOUR_MODEL_NAME>` - User defined model name that uniquely identifies the model
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the model version
* `<YOUR_RESOURCE_ID>` - Id that uniquely identifies the uploaded model, resource-id is generated and returned by the `resource-upload` command
```
pipeline resource-optimize-and-train --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type train --name <YOUR_MODEL_NAME> --tag <YOUR_TAG_NAME> --resource-subtype tensorflow --runtime-list [python] --chip-list [cpu] --resource-id <YOUR_RESOURCE_ID>
```
