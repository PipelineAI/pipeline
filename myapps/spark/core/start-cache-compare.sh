#!/bin/bash
#
echo '...Make Sure You Have Called RUNME_ONCE.sh to Setup the Required Linux Tools or This Script Will Fail...' 

echo '...Starting Perf Test (Cache Friendly Matrix Multiply)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.cache.CacheFriendlyMatrixMultiply 4096 1"

echo '...Starting Perf Test (Cache Naive Matrix Multiply)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.cache.CacheNaiveMatrixMultiply 4096 1"

