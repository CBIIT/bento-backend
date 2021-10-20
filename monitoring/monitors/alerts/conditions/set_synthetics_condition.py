#!/usr/bin/python

import json
import requests

def setsyntheticscondition(project, tier, key, synthetics_id, policy_id):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions.json'

   condition_name = '{}-{} Url Condition'.format(project.title(), tier.title())
   condition_found = False
   headers = {'Api-Key': key}
   data = {'policy_id': policy_id}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   
   for x in response.json()['synthetics_conditions']:
     if condition_name in x.get("name", "none"):
       condition_found = True
       condition_id = x.get("id", "none")

   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   data = {
     "synthetics_condition": {
       "name": condition_name,
       "monitor_id": synthetics_id,
       "enabled": True
     }
   }

   if not condition_found:
     # create condition
     API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions/policies'
     
     try:
       response = requests.post('{}/{}.json'.format(API_ENDPOINT, policy_id), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     print('{} Created'.format(condition_name))

   else:
     print('{} already exists - updating with the latest configuration'.format(condition_name))
     
     API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions/{}.json'.format(condition_id)

     # update condition
     try:
       response = requests.put('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)