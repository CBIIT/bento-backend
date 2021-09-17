#!/usr/bin/python

import json
import requests

def getsyntheticsconditions(policy, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   if response.json()['synthetics_conditions']:
     print('    Alert Synthetics Conditions:')
     for x in response.json()['synthetics_conditions']:
       print('    - ' + x.get("name", "none"))
     print()