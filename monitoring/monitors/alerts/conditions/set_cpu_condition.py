#!/usr/bin/python

import json
import requests

def setcpucondition(key, host, policy_id):

   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions?policy_id={}'.format(policy_id)

   condition_name = '{} CPU Used Condition'.format(host.title())
   condition_found = False
   headers = {'Api-Key': key}
   data = {'policy_id': policy_id}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['data']:
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
      "name":'{}-updated'.format(condition_name),
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