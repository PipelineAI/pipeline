#!/bin/bash
#
echo '...Make Sure You Have Called RUNME_ONCE.sh to Setup the Required Linux Tools or This Script Will Fail...'

echo '...Starting Perf Test (Synchronized Immutable Counters)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.thread.SynchronizedImmutableCounters 25000000 3 2"

echo '...Starting Perf Test (Synchronized Mutable Counters)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.thread.SynchronizedMutableCounters 25000000 3 2"

echo '...Starting Perf Test (Lock Free Atomic Reference)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.thread.LockFreeAtomicReferenceCounters 25000000 3 2"

echo '...Starting Perf Test (Lock Free Atomic Long)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.core.thread.LockFreeAtomicLongCounters 25000000 3 2"
