#!/bin/bash
#
echo '...Make Sure You Have Called RUNME_ONCE.sh to Setup the Required Linux Tools or This Script Will Fail...'

echo '...Starting Perf Test (Cache Friendly Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -Xmx16G -Xms10G -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.sort.CacheFriendlySort 10 100"

echo '...Starting Perf Test (Cache Naive Sort)...'
perf stat --repeat 1 --big-num --verbose --scale --event L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses java -Xmx16G -Xms10G -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.sort.CacheNaiveSort 10 100"
