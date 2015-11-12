#!/bin/bash
#
echo '...Building Tungsten Package...'
sbt tungsten/package

echo '...Starting Perf Test (Cache Friendly)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.tuple.CacheFriendlyTupleIncrement 25000000 3 2"

echo '...Starting Perf Test (Cache Naive)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.tuple.CacheNaiveTupleIncrement 25000000 3 2"

echo '...Starting Perf Test (Cache Super Naive)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.tuple.CacheSuperNaiveTupleIncrement 25000000 3 2"
