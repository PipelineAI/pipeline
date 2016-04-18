from flask import Flask, json, request
import requests
import numpy as np

app = Flask(__name__)

@app.route('/item-factors/<item_id>')
def item_factors(item_id):
    item_factors_json = requests.get("http://127.0.0.1:9200/advancedspark/item-factors-als/_search?q=itemId:%s" % item_id).json()
    return json.dumps(item_factors_json['hits']['hits'][0]['_source']['itemFeatures'])

@app.route('/user-factors/<user_id>')
def user_factors(user_id):
    user_factors_json = requests.get("http://127.0.0.1:9200/advancedspark/user-factors-als/_search?q=userId:%s" % user_id).json()
    return json.dumps(user_factors_json['hits']['hits'][0]['_source']['userFeatures'])

@app.route('/predict/<user_id>/<item_id>')
def predict(user_id, item_id):
    user_factor = np.fromstring(user_factors(user_id)[1:-1], dtype=float, sep=',')
    item_factor = np.fromstring(item_factors(item_id)[1:-1], dtype=float, sep=",")
    return json.dumps(user_factor.dot(item_factor))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5090')
