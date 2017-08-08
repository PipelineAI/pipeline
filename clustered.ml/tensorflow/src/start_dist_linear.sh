#!/bin/bash

# Parameter Servers
nohup python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=ps \
     --task_index=0 > ps0.out &

# Workers 

# Chief
python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=worker \
     --task_index=0 &

python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=worker \
     --task_index=1 &

python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=worker \
     --task_index=2 &

python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=worker \
     --task_index=3 & 

python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223,localhost:2224,localhost:2225,localhost:2226,localhost:2227 \
     --job_name=worker \
     --task_index=4 &


