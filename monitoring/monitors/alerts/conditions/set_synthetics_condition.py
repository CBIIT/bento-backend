#!/usr/bin/python

import json
import requests

def setsyntheticscondition(project, tier, key, synthetics_id, policy_id):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions/policies'
   
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   data = {
     "synthetics_condition": {
       "name": '{}-{}-url'.format(project, tier),
       "monitor_id": synthetics_id,
       "enabled": True
     }
   }

   response = requests.post('{}/{}.json'.format(API_ENDPOINT, policy_id), headers=headers, data=json.dumps(data), allow_redirects=False)
   print(response.text)