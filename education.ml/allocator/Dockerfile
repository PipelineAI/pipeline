FROM fluxcapacitor/package-ubuntu-16.04

WORKDIR /root

COPY allocator.py 
COPY data.csv 

EXPOSE 5070 

CMD ["supervise", "."]
