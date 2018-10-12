
![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

### PipelineAI Quick Start (CPU + GPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)

# Having Issues?  Contact Us Anytime... We're Always Awake.
* Slack:  https://joinslack.pipeline.ai
* Email:  [help@pipeline.ai](mailto:help@pipeline.ai)
* Web:  https://support.pipeline.ai
* YouTube:  https://youtube.pipeline.ai
* Slideshare:  https://slideshare.pipeline.ai
* [Troubleshooting Guide](/docs/troubleshooting)

# Troubleshooting

## Install Tools
* [Docker](https://www.docker.com/community-edition#/download)
* Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* (Windows Only) [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation)

## GPUs
* For GPU-based models, make sure you specify `--model-chip=gpu` or `--start-cmd=nvidia-docker` where appropriate 
* For GPU-based models, sure you have `nvidia-docker` installed!

## Gathering More Information
* If your server starts, try running `pipeline predict-server-logs` or `pipeline train-server-logs` to collect the startup logs.
* Please send all relevant information to PipelineAI [Support](#pipelineai-24x7-support) (Slack, Email, or Web).

## UnicodeDecodeError: 'ascii' codec can't decode byte 0xa0 in position 40: ordinal not in range(128)
* You likely have a pickling issue.
* Make sure that you are using Python 3.  We see issues when training on Python 2 (your environment), then unpickling with Python 3 (PipelineAI Model Runtime Environment)
* Also, we've found the `cloudpickle` library to be the most stable (versus Python's default `pickle` and `dill`.)

## WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. (`*-start`)
* Just ignore this

## `localhost:<port>` Not Working
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  
* This usually happens when using Docker Quick Terminal on Windows 7.

## PermissionError: [Errno 13] Permission denied: '...'
* Likely an issue installing the PipelineCLI 
* The cli requires Python 2 or 3 - preferably [Miniconda](https://conda.io/docs/user-guide/install/index.html#id2)
* Try `sudo pip install ...`
* Try `pip install --user ...`

## Could not delete '/usr/local/lib/python3.5/dist-packages/markupsafe/_speedups.cpython-35m-x86_64-linux-gnu.so': Permission denied
* Try `sudo pip install ...`

## Training job doesn't start
* You may need to clear the `--model_dir` before you start, otherwise the training job may not start

## `Exceeded 10% Allocated Memory`
* Increase `--memory-limit` above `2G`

## `Error response from daemon: Conflict. The container name "/train-mnist-v3" is already in use by container. You have to remove (or rename) that container to be able to reuse that name.`
* Stop and remove the container using `pipeline train-server-stop --model-name=mnist --model-tag=v3`

## Fixing Docker No Space Left on Device Error
http://phutchins.com/blog/2017/01/04/fixing-docker-no-space-left-on-device/

## "No space left on device Docker" (even though you have plenty of space)
* You may be seeing [THIS](https://stackoverflow.com/questions/44664900/oserror-errno-28-no-space-left-on-device-docker-but-i-have-space) issue with "No space left on device Docker"
* This happens when certain models - particularly parallelized Scikit-Learn models.
* The workaround is to add `--start-cmd-extra-args="--shm-size=512m"` to the `train-server-start` PipelineAI CLI command.

## Latest PipelineCLI Version is Not Installing Properly
* You need Python 2 or 3 (Conda Preferred)
* (Windows Only) [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

Try the following
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.5.xx <-- Exact Version You Are Expecting
```
```
sudo pip uninstall cli-pipeline
```
```
which pipeline
``` 
(or otherwise find which `pipeline` binary is being used.)
```
rm <bad-version-of-pipeline>
```
```
ll /usr/local/lib/python3.6/dist-packages/cli-pipeline*
```
```
rm -rf /usr/local/lib/python3.6/dist-packages/cli-pipeline*
```
```
ll ~/.local/lib/python3.6/site-packages/cli_pipeline*
```
```
rm -rf ~/.local/lib/python3.6/site-packages/cli_pipeline*
```
* Re-install `pip install cli-pipeline==...`
* If you have any issues, you may want to create a separate virtualenv or conda environment to isolate the environments.
* You may need to run `pip uninstall -y python-dateutil` if you see an issue related to `pip._vendor.pkg_resources.ContextualVersionConflict`
* You may also use `--user` if you're still having issues.
* If you see `xcrun: error: invalid active developer path (/Library/Developer/CommandLineTools), missing xcrun at: /Library/Developer/CommandLineTools/usr/bin/xcrun error: command '/usr/bin/clang' failed with exit status 1`, Follow these steps described here: https://apple.stackexchange.com/questions/254380/macos-sierra-invalid-active-developer-path:  
* `sudo xcode-select --reset` (didn't work for me, but including this because it worked for others) 
* `xcode-select --install`

## Paths
* `--model-path` needs to be relative
* On Windows, be sure to use the forward slash `\` for your paths.

## Training Args and Paths
* If you change the model (`pipeline_train.py`), you'll need to re-run `pipeline train-server-build ...`
* `--input-host-path` and `--output-host-path` are host paths (outside the Docker container) mapped inside the Docker container as `/opt/ml/input` (PIPELINE_INPUT_PATH) and `/opt/ml/output` (PIPELINE_OUTPUT_PATH) respectively.
* PIPELINE_INPUT_PATH and PIPELINE_OUTPUT_PATH are environment variables accesible by your model inside the Docker container. 
* PIPELINE_INPUT_PATH and PIPELINE_OUTPUT_PATH are hard-coded to `/opt/ml/input` and `/opt/ml/output`, respectively, inside the Docker conatiner .
* `--input-host-path` and `--output-host-path` should be absolute paths that are valid on the HOST Kubernetes Node
* Avoid relative paths for * `--input-host-path` and `--output-host-path` unless you're sure the same path exists on the Kubernetes Node 
* If you use `~` and `.` and other relative path specifiers, note that `--input-host-path` and `--output-host-path` will be expanded to the absolute path of the filesystem where this command is run - this is likely not the same filesystem path as the Kubernetes Node!
* `--input-host-path` and `--output-host-path` are available outside of the Docker container as Docker volumes
* `--train-args` is used to specify relative paths
* The values within `--train-args` are not expanded like other `-host-path` args
* Inside the model, you should use PIPELINE_INPUT_PATH (`/opt/ml/input`) as the base path for the subpaths defined in `--train-files` and `--eval-files`
* You can use our samples by setting `--input-host-path` to anything (ignore it, basically) and using an absolute path for `--train-files`, `--eval-files`, and other args referenced by your model
* You can specify S3 buckets/paths in your `--train-args`, but the host Kubernetes Node needs to have the proper EC2 IAM Instance Profile needed to access the S3 bucket/path
* Otherwise, you can specify ACCESS_KEY_ID and SECRET_ACCESS_KEY in your model code (not recommended_
* `--train-files` and `--eval-files` can be relative to PIPELINE_INPUT_PATH (`/opt/ml/input`), but remember that PIPELINE_INPUT_PATH is mapped to PIPELINE_HOST_INPUT_PATH which must exist on the Kubernetes Node where this container is placed (anywhere)
* `--train-files` and `--eval-files` are used by the model, itself
* You can pass any parameter into `--train-args` to be used by the model (`pipeline_train.py`)
* `--train-args` is a single argument passed into the `pipeline_train.py`
* Models, logs, and event are written to `--output-host-path` (or a subdirectory within it).  These paths are available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-host-path` between calls or 2) create a new unique subfolder within `--output-host-path` in your `pipeline_train.py` (ie. timestamp).
* Make sure you use a consistent `--output-host-path` across nodes.  If you use timestamp, for example, the nodes in your distributed training cluster will not write to the same path.  You will see weird ABORT errors from TensorFlow.
* On Windows, be sure to use the forward slash `\` for `--input-host-path` and `--output-host-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-mnist-v3`.
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!
* For GPU-based models, make sure you specify `--model-chip=gpu`

## Http/Https Proxy
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url` or `[Errno 111] Connection refused'` or `ConnectionError(MaxRetryError("HTTPSConnectionPool`, you need to update `./tensorflow/census/model/pipeline_condarc` to include proxy servers per [THIS](https://conda.io/docs/user-guide/configuration/use-condarc.html#configure-conda-for-use-behind-a-proxy-server-proxy-servers) document.
* For `pip` installs, you may also need to `export HTTP_PROXY` and `export HTTPS_PROXY` within `./tensorflow/census/model/pipeline_setup.sh`
* You may also need to set the lower-case version, as well:  `export http_proxy` and `export https_proxy`
* And if your proxy server password uses special characters (ie. `@`, you may need to convert the characters to [ASCII](https://www.ascii-code.com/) (ie. `%40`).
* You may need to remove the `!#/bin/bash`
* Lastly, you may need to set shell-wide (`~/.bashrc`) and re-source (`. ~/.bashrc`) - or set them as system-wide environment variables (` `) per [THIS](https://help.ubuntu.com/community/EnvironmentVariables#System-wide_environment_variables) document.

## `/pipeline_setup.sh: no such file or directory`
* During `predict-server-build`, you are not pointing `--model-path` to  `/model` subdirectory of the parent model directory.
* Example: `COPY python/gitstar/pipeline_setup.sh $PIPELINE_MODEL_PATH/pipeline_setup.sh
COPY failed: stat /var/lib/docker/tmp/docker-builder805520280/python/gitstar/pipeline_setup.sh: no such file or directory`

## PipelineAI 24x7 Support
* [PipelineAI Slack](https://joinslack.pipeline.ai)
* [help@pipeline.ai](mailto:help@pipeline.ai)
* [Web](https://support.pipeline.ai)
* [Troubleshooting Guide](/docs/troubleshooting)
