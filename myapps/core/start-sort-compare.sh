#!/bin/bash
#
echo '...Building Package...'
sbt package

echo '...Starting Perf Test (Cache Friendly Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main org.apache.spark.util.collection.CacheFriendlySort 10 10"

echo '...Starting Perf Test (Cache Naive Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main org.apache.spark.util.collection.CacheNaiveSort 10 10"
