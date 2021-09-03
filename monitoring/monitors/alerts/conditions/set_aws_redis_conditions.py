#!/usr/bin/python

import json
import requests

def setawsredisconditions(key, project, tier, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }

   host_query = "(displayName LIKE '{}-{}-redis-cluster-%')".format(project, tier)

   # set redis system alerts
   condition_name = '{}-{} AWS Redis Memory Condition'.format(project.title(), tier.title())
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "integration_provider":"ElastiCacheRedisCluster",
      "event_type":"DatastoreSample",
      "select_value":"provider.databaseMemoryUsagePercentage.Average",
      "comparison":"above",
      "critical_threshold":{
         "value":1,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":0.8,
         "duration_minutes":10,
         "time_function":"any"
      }
     }
   }

   try:
     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))
   
   # set redis performance alerts
   condition_name = '{}-{} AWS Redis Command Condition'.format(project.title(), tier.title())
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

   try:
     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))