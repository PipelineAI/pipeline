!/bin/bash

# Args
#   $1:  zone (us-west1-b (230), us-east1-d (50), europe-west1-b (50), asia-east1-a (50))

for idx in `seq 1 10`;
do
  nohup ./create_instance.sh $1 $idx > a-$1-$idx.out &
done
