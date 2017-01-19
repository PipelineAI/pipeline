#!/bin/bash
curl -u myusername:mypassword -H "Content-Type: application/json" -X POST -d '{
  "name": "web",
  "active": true,
  "events": ["push"],
  "config": {
    "url": "http://airflow.demo.pipeline.io/github/webhook",
    "content_type": "json"    
  }
}' https://api.github.com/repos/fluxcapacitor/pipeline/hooks
