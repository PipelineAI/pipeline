#!/bin/bash
curl -u myusername:mypassword -H "Content-Type: application/json" -X POST -d '{
  "name": "web",
  "active": true,
  "config": {
    "url": "http://demo.pipeline.io:5000/prediction.ml/pmml/data/census/webhook",
    "content_type": "json"
  }
}' https://api.github.com/repos/fluxcapacitor/pipeline/hooks
