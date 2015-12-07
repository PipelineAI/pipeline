#!/bin/bash
#
echo '...Building Package...'
sbt package

echo '...Starting Perf Test (Cache Friendly Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.sort.CacheFriendlySort 10 10"

echo '...Starting Perf Test (Cache Naive Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.sort.CacheNaiveSort 10 10"
