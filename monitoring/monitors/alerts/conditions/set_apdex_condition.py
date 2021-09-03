#!/usr/bin/python

import json
import requests
from monitors.apm import get_apm_apps

def setapdexcondition(project, tier, key, policy_id):

   # get apm app ids
   API_ENDPOINT = 'https://api.newrelic.com/v2/applications.json'

   headers = {'Api-Key': key}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   
   apm_id=[]
   for x in response.json()['applications']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       apm_id.append(x.get("id", "none"))

   #set apdex for apm apps
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions/policies'

   condition_name = '{}-{} APM Apdex'.format(project.title(), tier.title())
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   data = {
     "condition": {
       "type": "apm_app_metric",
       "name": condition_name,
       "enabled": True,
       "entities": apm_id,
       "metric": "apdex",
       "condition_scope": "application",
       "terms": [
         {
           "duration": "5",
           "operator": "below",
           "priority": "critical",
           "threshold": "0.7",
           "time_function": "all"
         },
         {
           "duration": "5",
           "operator": "below",
           "priority": "warning",
           "threshold": "0.8",
           "time_function": "all"
         }
       ]
     }
   }

   try:
     response = requests.post('{}/{}.json'.format(API_ENDPOINT, policy_id), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))