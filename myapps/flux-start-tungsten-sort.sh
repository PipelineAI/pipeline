#!/bin/bash
#
echo '...Building Tungsten Package...'
sbt tungsten/package

echo '...Starting Tungsten WholeRecordSort...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main org.apache.spark.util.collection.WholeRecordSort $DATASETS_HOME/tungsten/tungsten.dat"

echo '...Starting Tungsten KeyPointerSort...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main org.apache.spark.util.collection.KeyPointerSort $DATASETS_HOME/tungsten/tungsten.dat"

echo '...Starting Tungsten KeyPrefixPointerSort...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main org.apache.spark.util.collection.KeyPrefixPointerSort $DATASETS_HOME/tungsten/tungsten.dat"

echo '...Starting Tungsten UnsafeKeyPrefixPointerSort...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main org.apache.spark.util.collection.UnsafeKeyPrefixPointerSort $DATASETS_HOME/tungsten/tungsten.dat"
