#!/usr/bin/python

import json
import requests

def setsyntheticscondition(project, tier, key, synthetics_id, policy_id):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions/policies'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   condition_name = '{}-{} Url Condition'.format(project, tier)
   data = {
     "synthetics_condition": {
       "name": condition_name,
       "monitor_id": synthetics_id,
       "enabled": True
     }
   }

   try:
     response = requests.post('{}/{}.json'.format(API_ENDPOINT, policy_id), headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(condition_name))