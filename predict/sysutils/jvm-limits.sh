#!/bin/sh
# =================================================================
# Detect whether running in a container and set appropriate options
# for limiting Java VM resources
#
# Usage: JAVA_OPTIONS="$(jvm-limits.sh)"

# Env Vars evaluated:

# JAVA_OPTIONS: Checked for already set options
# JAVA_MAX_MEM_RATIO: Ratio use to calculate a default maximum Memory, in percent.
#                     E.g. the default value "50" implies that 50% of the Memory
#                     given to the container is used as the maximum heap memory with
#                     '-Xmx'. It is a heuristic and should be better backed up with real
#                     experiments and measurements.
#                     For a good overviews what tuning options are available -->
#                             https://youtu.be/Vt4G-pHXfs4
#                             https://www.youtube.com/watch?v=w1rZOY5gbvk
#                             https://vimeo.com/album/4133413/video/181900266
# Also note that heap is only a small portion of the memory used by a JVM. There are lot
# of other memory areas (metadata, thread, code cache, ...) which addes to the overall
# size. There is no easy solution for this, 50% seems to be are reasonable compromise.
# However, when your container gets killed because of an OOM, then you should tune
# the absolute values
#

# Check for memory options and calculate a sane default if not given
max_memory() {
  # Check whether -Xmx is already given in JAVA_OPTIONS. Then we dont
  # do anything here
  if echo "${JAVA_OPTIONS}" | grep -q -- "-Xmx"; then
    return
  fi

  # Check if explicitely disabled
  if [ "x$JAVA_MAX_MEM_RATIO" = "x0" ]; then
    return
  fi

  # Check for the 'real memory size' and caluclate mx from a ratio
  # given (default is 50%)
  if [ "x$CONTAINER_MAX_MEMORY" != x ]; then
    local max_mem="${CONTAINER_MAX_MEMORY}"
    local ratio=${JAVA_MAX_MEM_RATIO:-50}
    local mx=$(echo "${max_mem} ${ratio} 1048576" | awk '{printf "%d\n" , ($1*$2)/(100*$3) + 0.5}')
    echo "-Xmx${mx}m"
  fi
}

# Switch on diagnostics except when switched off
diagnostics() {
  if [ "x$JAVA_DIAGNOSTICS" != "x" ]; then
    echo "-XX:NativeMemoryTracking=summary -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UnlockDiagnosticVMOptions"
  fi
}

cpu_core_tunning() {
  local core_limit="${JAVA_CORE_LIMIT}"
  if [ "x$core_limit" = "x0" ]; then
    return
  fi

  if [ "x$CONTAINER_CORE_LIMIT" != x ]; then
    if [ "x$core_limit" = x ]; then
      core_limit="${CONTAINER_CORE_LIMIT}"
    fi
    echo "-XX:ParallelGCThreads=${core_limit} " \
         "-XX:ConcGCThreads=${core_limit} " \
         "-Djava.util.concurrent.ForkJoinPool.common.parallelism=${core_limit}"
  fi
}

# Echo options, trimming trailing and multiple spaces
echo "$(max_memory) $(diagnostics) $(cpu_core_tunning)" | awk '$1=$1'
