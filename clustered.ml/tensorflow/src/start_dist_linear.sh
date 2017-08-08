rm ps.out

nohup python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223 \
     --job_name=ps \
     --task_index=0 > ps.out &

python dist_linear.py \
     --ps_hosts=localhost:2222 \
     --worker_hosts=localhost:2223 \
     --job_name=worker \
     --task_index=0 
