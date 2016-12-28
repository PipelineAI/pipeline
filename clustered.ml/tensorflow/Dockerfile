FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

RUN \
  apt-get update 

ENV \
  TENSORFLOW_VERSION=0.12.0

RUN \
  pip install https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-$TENSORFLOW_VERSION-cp35-cp35m-linux_x86_64.whl

COPY bin/ bin/
COPY data/ data/

COPY run run

EXPOSE 2222

CMD ["supervise", "."]
