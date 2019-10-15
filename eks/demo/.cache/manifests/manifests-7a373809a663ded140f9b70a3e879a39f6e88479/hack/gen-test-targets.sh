#!/usr/bin/env bash
#
# gen-test-targets will generate units tests under tests/ for all directories that
# have a kustomization.yaml. This script first finds all directories and then calls
# gen-test-target to generate each golang unit test.
# The script is based on kusttestharness_test.go from kubernetes-sigs/pkg/kusttest/kusttestharness.go
#
if [[ $(basename $PWD) != "manifests" ]]; then
  echo "must be at manifests root directory to run $0"
  exit 1
fi

EXCLUDE_DIRS=( "kfdef" "gatekeeper" "gcp/deployment_manager_configs" "aws/infra_configs" )
source hack/utils.sh
rm -f $(ls tests/*_test.go | grep -v kusttestharness_test.go)
for i in $(find * -type d -exec sh -c '(ls -p "{}"|grep />/dev/null)||echo "{}"' \; | egrep -v 'doc|tests|hack|plugins'); do
  exclude=false
  for item in "${EXCLUDE_DIRS[@]}"
  do
  	#https://stackoverflow.com/questions/2172352/in-bash-how-can-i-check-if-a-string-begins-with-some-value
  	# Check if item is a prefix of i
    if [[ "$i" == "$item"* ]]; then
        exclude=true
    fi
  done

  if $exclude; then
    continue
  fi
  rootdir=$(pwd)
  absdir=$rootdir/$i  
  if [[ ! $absdir  =~ overlays/test$ ]]; then
    testname=$(get-target-name $absdir)_test.go
    echo generating $testname from manifests/${absdir#*manifests/}
    ./hack/gen-test-target.sh $absdir 1> tests/$testname
  fi
done
