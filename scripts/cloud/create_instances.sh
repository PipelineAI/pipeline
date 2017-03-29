!/bin/bash

# Args
#   $1:  zone (us-west1-b, us-east1-d, europe-west1-b, asia-east1-a)

for idx in `seq 1 10`;
do
  nohup ./create_instance.sh $1 $idx > a-$1-$idx.out &
done
