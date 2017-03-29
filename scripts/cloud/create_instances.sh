!/bin/bash

# Args:
#   $1: zone (ie. us-west1-b)

for idx in `seq 1 10`;
do
  nohup ./create_instance.sh $1 $idx > a-$idx.out &
done
