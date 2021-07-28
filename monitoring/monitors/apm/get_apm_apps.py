#!/usr/bin/python

import json
import requests

def getapmapps(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/applications.json'

   headers = {'Api-Key': key}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   print('APM Applications:')
   for x in response.json()['applications']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
   print()