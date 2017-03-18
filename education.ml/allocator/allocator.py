import subprocess

from flask import Flask, json, request, after_this_request, jsonify
import requests
import random
import sys
import csv
import threading
from time import gmtime, strftime

app = Flask(__name__)

global index
global max_index
global data
global output_file
global output_filewriter
global allocated_data

@app.route('/whoami')
def whoami():
  return jsonify({"whoami_ip": request.remote_addr})

@app.route('/allocate')
def allocate():
  global index
  global data

  lock = threading.Lock()

  lock.acquire()
  time = strftime("%Y-%m-%d-%H:%M:%S", gmtime())
  if (index <= max_index):
    override = request.args.get('override', 'false')
    if (request.remote_addr in allocated_data and override == 'false'):
      datum = "ERROR_DUPLICATE_ALLOCATION_REQUEST"
    else:
      datum = data[index]
  else:
    datum = "ERROR_MAX_INDEX_REACHED"

  print("(%s, %s, %s, %s)" % (time, request.remote_addr, index, datum))

  output_filewriter.writerow([time, request.remote_addr, index, datum])

  json_result = jsonify({"allocation": datum}, {"index": index}, {"time": time})

  if (datum not in ("ERROR_DUPLICATE_ALLOCATION_REQUEST", "ERROR_MAX_INDEX_REACHED")):
    allocated_data[request.remote_addr] = datum
    index = index + 1

  lock.release()

  return json_result 

@app.route('/close')
def close():
  output_file.close()
  return jsonify({"OK"})

if __name__ == '__main__':
  global index
  global max_index
  global output_file
  global output_filewriter
  global data
  global allocated_data

  input_filename = "data.csv"

  input_file = open(input_filename, 'rt')

  data = list(csv.reader(input_file))
  max_index = len(data) - 1

  allocated_data = {}

  lock = threading.Lock()
  lock.acquire()
  index = 0
  lock.release()

  for datum in data:
    print(datum)

  output_filename = "allocated_data.csv"
  output_file = open(output_filename, 'wt')
  output_filewriter = csv.writer(output_file)

  input_file.close()

  app.run(host='0.0.0.0', port='5070')
