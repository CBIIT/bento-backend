#!/usr/bin/python

import json
import requests

def setawsredisconditions(key, project, tier, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions?policy_id={}'.format(policy_id)

   condition_name = '{}-{} AWS Redis Command Condition'.format(project.title(), tier.title())
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

   host_query = "(displayName LIKE '{}-{}-redis-cluster-%')".format(project, tier)

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
      "integration_provider":"ElastiCacheRedisNode",
      "event_type":"DatastoreSample",
      "select_value":"provider.stringBasedCmds.Average",
      "comparison":"above",
      "critical_threshold":{
         "value":10,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":5,
         "duration_minutes":10,
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
     
     API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions/{}'.format(condition_id)

     # update condition
     try:
       response = requests.put('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)