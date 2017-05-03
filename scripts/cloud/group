#!/bin/bash

# Args
#  $1: instance_group_name
#  $2: zone (us-west1-b (230), us-east1-d (50), europe-west1-b (50), asia-east1-a (50))
#  $3: num_instances

gcloud compute instance-groups unmanaged create $1 --zone=$2

for idx in `seq 1 $3`;
do
  nohup gcloud compute instance-groups unmanaged add-instances $1 --zone=$2 --instances=$1-$idx.out > a-$1-$idx.out &
done
