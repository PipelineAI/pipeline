cd $PIPELINE_HOME/myapps

perf stat -B -e L1-dcache-load-misses,L1-dcache-prefetch-misses,LLC-loads,LLC-load-misses,LLC-prefetches,LLC-prefetch-misses,cache-references,cache-misses java -server -XX:-TieredCompilation -Xmx13G -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G -XX:ReservedCodeCacheSize=512m -XX:MaxPermSize=1024m -jar ~/sbt/bin/sbt-launch.jar "tungsten/run-main com.advancedspark.tungsten.matrix.CacheFriendlyMatrixMultiply 256 1"

cd $PIPELINE_HOME
