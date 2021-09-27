#!/usr/bin/python

import json
import requests

def setnginxconditions(key, project, tier, policy_id):
   API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts/conditions'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }

   host_query = "(label.Project = '{}' AND label.Environment = '{}')".format(project, tier)

   # set nginx performance alerts
   condition_name = '{}-{} Nginx Performance Condition'.format(project.title(), tier.title())
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"NginxSample",
      "select_value":"net.connectionsWaiting",
      "comparison":"above",
      "critical_threshold":{
         "value":2,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":1,
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

   # set nginx error alerts
   condition_name = '{}-{} Nginx Error Condition'.format(project.title(), tier.title())
   data = {
     "data":{
      "type":"infra_metric",
      "name":condition_name,
      "enabled":True,
      "where_clause":host_query,
      "policy_id":policy_id,
      "event_type":"NginxSample",
      "select_value":"net.connectionsDroppedPerSecond",
      "comparison":"above",
      "critical_threshold":{
         "value":0,
         "duration_minutes":5,
         "time_function":"any"
      },
      "warning_threshold":{
         "value":0,
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