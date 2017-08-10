#!/bin/bash

# Parameter Servers
nohup python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=ps \
     --task_index=0 > ps0.out &

nohup python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=ps \
     --task_index=1 > ps1.out &

# Workers 

# Chief
python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=0 &

python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=1 &

python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=2 &

python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=3 & 

python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=4 &

python src/dist_linear_train.py \
     --ps_hosts=localhost:2222,localhost:2223 \
     --worker_hosts=localhost:3223,localhost:3224,localhost:3225,localhost:3226,localhost:3227,localhost:3228 \
     --job_name=worker \
     --task_index=5 &
