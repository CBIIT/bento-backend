#!/usr/bin/python

import json
import requests

def setdiskspacecondition(key, host, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   host_query = "(displayName IN ('{}'))".format(host)

   condition_name = '{} Disk Space Condition'.format(host)
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"StorageSample",
      "select_value":"diskFreePercent",
      "comparison":"below",
      "critical_threshold":{
         "value":10,
         "duration_minutes":1,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":30,
         "duration_minutes":2,
         "time_function":"any"
      }
     }
   }

   try:
     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))