
#!/usr/bin/env bash
# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================

echo '*** YOUR HOST MACHINE MUST HAVE A GPU WITH THE FOLLOWING SPECS ***'
echo '*** Nvidia >= 3.0 Compute Compabilitity, CUDA Toolkit >= 7.0, and cuDNN >= 2.0 ***' 

set -e

export CUDA_HOME=${CUDA_HOME:-/usr/local/cuda}

if [ ! -d ${CUDA_HOME}/lib64 ]; then
  echo "Failed to locate CUDA libs at ${CUDA_HOME}/lib64."
  exit 1
fi

export CUDA_SO=$(\ls /usr/lib/x86_64-linux-gnu/libcuda.* | \
                    xargs -I{} echo '-v {}:{}')
export DEVICES=$(\ls /dev/nvidia* | \
                    xargs -I{} echo '--device {}:{}')

if [[ "${DEVICES}" = "" ]]; then
  echo "Failed to locate NVidia device(s). Did you want the non-GPU container?"
  exit 1
fi

docker run -it $CUDA_SO $DEVICES "$@" --name pipeline -h docker -m 8g -p 80:80 -p 36042:6042 -p 39160:9160 -p 39042:9042 -p 39200:9200 -p 37077:7077 -p 38080:38080 -p 38081:38081 -p 36060:6060 -p 36061:6061 -p 36062:6062 -p 36063:6063 -p 36064:6064 -p 36065:6065 -p 32181:2181 -p 38090:8090 -p 30000:10000 -p 30070:50070 -p 30090:50090 -p 39092:9092 -p 36066:6066 -p 39000:9000 -p 39999:19999 -p 36081:6081 -p 35601:5601 -p 37979:7979 -p 38989:8989 -p 34040:4040 -p 34041:4041 -p 34042:4042 -p 34043:4043 -p 34044:4044 -p 34045:4045 -p 34046:4046 -p 34047:4047 -p 34048:4048 -p 34049:4049 -p 34050:4050 -p 34051:4051 -p 34052:4052 -p 34053:4053 -p 34054:4054 -p 34055:4055 -p 34056:4056 -p 34057:4057 -p 34058:4058 -p 34059:4059 -p 34060:4060 -p 36379:6379 -p 38888:8888 -p 34321:54321 -p 38099:8099 -p 38754:8754 -p 37379:7379 -p 36969:6969 -p 36970:6970 -p 36971:6971 -p 36972:6972 -p 36973:6973 -p 3694:6974 -p 36975:6975 -p 36976:6976 -p 36977:6977 -p 36978:6978 -p 36979:6979 -p 36980:6980 -p 35050:5050 -p 35060:5060 -p 37060:7060 -p 38182:8182 -p 39081:9081 -p 38998:8998 -p 39090:9090 fluxcapacitor/pipeline bash


