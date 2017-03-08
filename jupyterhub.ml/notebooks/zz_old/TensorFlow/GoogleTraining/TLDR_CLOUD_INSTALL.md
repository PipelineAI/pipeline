
# TensorFlow on Cloud Machine Learning: Setup in a cloud-based environment

If you’re unfamiliar with Google Cloud Platform, and/or would rather do the exercises in this workshop in a cloud environment, follow these steps for initial setup.

(Alternative setup flows for local installation can be found [here](INSTALL.md).)

Here's an overview of what you'll do:

- Create a new project (as necessary).
- Create a "container-optimized" VM and ssh to it.
- Start a docker container running on the VM. You'll find the example code in `tensorflow-workshop-master`.
- Then, in the running docker container, you'll do some auth and setup.

## Prerequsites:

- A credit card (you will not be charged), to create a Google Cloud Platform account; or, an existing account.
- Chrome, or another browser and an ssh client.


## Initial setup


### 1. Set Up Your GCP Project

#### 1.1 Create a Cloud Platform project

Create a Google Cloud Platform (GCP) account by [signing up for the free trial](https://cloud.google.com/free-trial/).
You will be asked to enter a credit card number, but you will get $300 of credits, and won't be billed.

If you already have an account, and have run through your trial credits, see one of the workshop instructors. We will give you additional credits to apply to your account.

#### 1.2 Enable the necessary APIs

1. Click on the “hamburger” menu at upper-left, and then “API Manager”.
1. On the left nav, choose "Dashboard" if not already selected, then choose "+Enable API" in the top-middle of page.
1. Enter "Google Compute Engine API" in the search box and click it when it appears in the list of results.
1. Click on “Enable” (top-middle of page).
1. Repeat steps 2 through 4 for: "Google Dataflow API" and "Google Cloud Machine Learning".

![Hamburger menu](./assets/hamburger.png)  

![API Manager](./assets/api_manager.png)


### 2. Connect to your project's Cloud Shell

Click on the Cloud Shell icon (leftmost icon in the set of icons at top-right of the page).

![Cloud Shell](./assets/cloudshell.png)


Run commands 3-6 below in the Cloud Shell.

### 3. Initialize Cloud ML for your project

```shell
gcloud beta ml init-project
```

Respond "Y" when asked.

### 4. Set up your Cloud Storage Bucket

```shell
PROJECT_ID=$(gcloud config list project --format "value(core.project)")
BUCKET_NAME=${PROJECT_ID}-ml
gsutil mb -l us-central1 gs://$BUCKET_NAME
```

### 5. Create a container-optimized image in GCE

```shell
gcloud compute instances create mlworkshop \
    --image-family gci-stable \
    --image-project google-containers \
    --zone us-central1-b --boot-disk-size=100GB \
    --machine-type n1-standard-1
```

You can ignore the "I/O performance warning for disks < 200GB" for this example; it is not important in this context.

### 6. Set up a firewall rule for your project that will allow access to the web services we will run

```shell
gcloud compute firewall-rules create mlworkshop --allow tcp:8888,tcp:6006,tcp:5000
```

### 7. SSH into the new GCE instance, in a new browser window

- Click on the “hamburger” menu at upper-left, and then “Compute Engine”
- Find your instance in the list (mid-page) and click on the “SSH” pulldown menu on the right. Select “Open in browser window”.
- A new browser window will open, with a command line into your GCE instance.

### 8. Start the Docker container in the GCE image (in the newly opened SSH browser window):

```shell
docker pull gcr.io/google-samples/tf-workshop:v5
mkdir workshop-data
docker run -v `pwd`/workshop-data:/root/tensorflow-workshop-master/workshop-data -it \
    -p 6006:6006 -p 8888:8888 -p 5000:5000 gcr.io/google-samples/tf-workshop:v5
```

Then, run the following in the Docker container.

### 9. Configure the Docker container. You’ll need your project ID for this step.

(If you have forgotten your project ID, you can find it in the console by selecting the Home Dashboard.  It will be listed near the upper left of the main panel.)

In the following, replace `<your-project-ID>` with your actual project ID.

```shell
gcloud config set project <your-project-ID>
gcloud config set compute/region us-central1
gcloud config set compute/zone us-central1-b
PROJECT_ID=$(gcloud config list project --format "value(core.project)")
BUCKET=gs://${PROJECT_ID}-ml
```

```shell
gcloud auth login
```
(and follow the subsequent instructions)

```shell
gcloud beta auth application-default login
```
(and follow the subsequent instructions)

### 10. Copy some data for the 'transfer learning' example to your bucket:

```shell
gsutil cp -r gs://tf-ml-workshop/transfer_learning/hugs_preproc_tfrecords $BUCKET
GCS_PATH=$BUCKET/hugs_preproc_tfrecords
```

## If you need to restart the container later

If you later exit your container and then want to restart it again, you can find the container ID by running the following in your VM:

```shell
docker ps -a
docker start <container_id>
```
Once the workshop container is running again, you can exec back into it like this:

```shell
docker exec -it <container_id> bash
```

Note that you may need to define environment variables from step 9 when you reconnect.
Note also that if you later start a separate new container 'from scratch', you will need to repeat the auth setup.

## What next?

You should now be set to run all of the workshop exercises from your docker container!
Change to the `tensorflow-workshop-master` directory.

Some of the labs have you run a jupyter or Tensorboard server.  Instead of using 'localhost', use the assigned IP for your VM.  You can find it in the cloud console by visiting the Compute Engine > VM Instances panel.

## Cleanup

Once you’re done with your VM, you can stop or delete it. If you think you might return to it later, you might prefer to just stop it. (A stopped instance does not incur charges, but all of the resources that are attached to the instance will still be charged).  You can do this from the [cloud console](https://console.cloud.google.com), or via command line from the Cloud Shell as follows:

```shell
gcloud compute instances delete --zone us-central1-b mlworkshop
```
Or:

```shell
gcloud compute instances stop --zone us-central1-b mlworkshop
```
Then later:

```shell
gcloud compute instances start --zone us-central1-b mlworkshop
```
Delete the firewall rule as well:

```shell
gcloud compute firewall-rules delete mlworkshop
```
