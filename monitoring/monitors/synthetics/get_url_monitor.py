#!/usr/bin/python

import json
import requests

def geturlmonitor(project, tier, key):
   API_ENDPOINT = 'https://synthetics.newrelic.com/synthetics/api'

   headers = {'Api-Key': key}

   try:
     response = requests.get('{}/v3/monitors'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   print('Synthetics Monitors:')
   for x in response.json()['monitors']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
   print()