#!/usr/bin/python

import json
import requests

def setcpucondition(key, host, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   host_query = "(displayName IN ('{}'))".format(host)
   
   condition_name = '{} CPU Used Condition'.format(host.title())
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"SystemSample",
      "select_value":"cpuPercent",
      "comparison":"above",
      "critical_threshold":{
         "value":80,
         "duration_minutes":5,
         "time_function":"all"
      },
      "warning_threshold":{
         "value":80,
         "duration_minutes":2,
         "time_function":"all"
      }
     }
   }

   try:
     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))