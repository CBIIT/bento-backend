#!/usr/bin/python

import json
import requests

def setdiskspacecondition(key, host, policy_id):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions.json'

   condition_name = '{} Disk Space Condition'.format(host.title())
   condition_found = False
   headers = {'Api-Key': key}
   data = {'policy_id': policy_id}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['conditions']:
     if condition_name in x.get("name", "none"):
       condition_found = True
       condition_id = x.get("id", "none")

   host_query = "(displayName IN ('{}'))".format(host)

   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
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

   if not condition_found:
     # create policy
     API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
     
     try:
       response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     print('{} Created'.format(condition_name))

   else:
     print('{} already exists - updating with the latest configuration'.format(condition_name))
     
     API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions/{}.json'.format(condition_id)

     # update condition
     try:
       response = requests.put('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)