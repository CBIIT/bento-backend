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

   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions.json'

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

   if not condition_found:
     # create condition
     API_ENDPOINT = 'https://infra-api.newrelic.com/v2/alerts_conditions/policies'
     
     try:
       response = requests.post('{}/{}.json'.format(API_ENDPOINT, policy_id), headers=headers, data=json.dumps(data), allow_redirects=False)
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