FROM fluxcapacitor/package-kafka-0.10:master

WORKDIR /root

ENV \
 TENSORFLOW_VERSION=1.0.1 \
 TENSORFLOW_SERVING_VERSION=0.5.1 \
 BAZEL_VERSION=0.4.4

RUN \
 apt-get update \
 && apt-get install -y unzip

RUN \
 wget https://github.com/bazelbuild/bazel/releases/download/$BAZEL_VERSION/bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
 && chmod +x bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
 && ./bazel-$BAZEL_VERSION-installer-linux-x86_64.sh --bin=/root/bazel-$BAZEL_VERSION/bin \
 && rm bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
 && export PATH=$PATH:/root/bazel-${BAZEL_VERSION}/bin/

RUN \
 pip install --upgrade https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-$TENSORFLOW_VERSION-cp27-none-linux_x86_64.whl

RUN \
# pip install grpcio \
 apt-get install -y \
   build-essential \
   libfreetype6-dev \
   libpng12-dev \
   libzmq3-dev \
   pkg-config \
   python-numpy \
   software-properties-common \
   swig \
   zip \
   zlib1g-dev

COPY src/ src/
COPY run run 

CMD ["supervise", "."]
