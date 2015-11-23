#!/bin/bash
#
echo '...Building Package...'
sbt package

echo '...Starting Perf Test (Lock Free)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.mechanicalsympathy.thread.LockFreeAtomicLong2CounterIncrement 25000000 3 2"

echo '...Starting Perf Test (Naive Tuple)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.mechanicalsympathy.thread.NaiveTuple2CounterIncrement 25000000 3 2"

echo '...Starting Perf Test (Naive Case Class)...'
echo '...YOU MUST HIT ctrl-c BETWEEN RUNS (AFTER THE OUTPUT SHOWS)...'
perf stat --repeat 1 --big-num --verbose --scale --event context-switches,L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-load-misses,LLC-prefetch-misses,cache-misses,stalled-cycles-frontend java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.mechanicalsympathy.thread.NaiveCaseClass2CounterIncrement 25000000 3 2"
