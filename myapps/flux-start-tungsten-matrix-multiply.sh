#!/bin/bash
#
echo '...Building Tungsten Package...'
sbt tungsten/package

echo '...Starting Perf Test (Cache Friendly)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.matrix.CacheFriendlyMatrixMultiply 4096 1"

echo '...Starting Perf Test (Cache Naive)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.matrix.CacheNaiveMatrixMultiply 4096 1"
