#!/usr/bin/python

import json
import requests

def setredisconditions(key, project, tier, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }

   host_query = "(label.Project = '{}' AND label.Environment = '{}')".format(project, tier)

   # set redis system alerts
   condition_name = '{}-{} Redis Memory Condition'.format(project, tier)
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"RedisSample",
      "select_value":"system.totalSystemMemoryBytes",
      "comparison":"below",
      "critical_threshold":{
         "value":2000000000,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":4000000000,
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
   condition_name = '{}-{} Redis Command Condition'.format(project, tier)
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"RedisSample",
      "select_value":"net.commandsProcessedPerSecond",
      "comparison":"below",
      "critical_threshold":{
         "value":0.1,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":0.2,
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

   # set redis error alerts
   condition_name = '{}-{} Redis Error Condition'.format(project, tier)
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"RedisSample",
      "select_value":"net.rejectedConnectionsPerSecond",
      "comparison":"above",
      "critical_threshold":{
         "value":0,
         "duration_minutes":5,
         "time_function":"any"
      }
     }
   }

   try:
     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))