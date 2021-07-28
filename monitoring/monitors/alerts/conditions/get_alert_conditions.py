#!/usr/bin/python

import json
import requests

def getalertconditions(policy, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['conditions']:
     print('    Alert Conditions:')
     for x in response.json()['conditions']:
       print('    - ' + x.get("name", "none"))
     print()