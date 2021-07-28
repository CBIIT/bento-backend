#!/usr/bin/python

import json
import requests

def getexternalservices(policy, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_external_service_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['external_service_conditions']:
     print('    Alert NRQL Conditions:')
     for x in response.json()['external_service_conditions']:
       print('    - ' + x.get("name", "none"))
     print()