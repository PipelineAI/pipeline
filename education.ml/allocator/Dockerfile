FROM fluxcapacitor/package-ubuntu-16.04

RUN \
  pip install flask requests

WORKDIR /root

COPY allocator.py allocator.py 
COPY data.csv data.csv
COPY run run 

EXPOSE 5070 

CMD ["supervise", "."]
