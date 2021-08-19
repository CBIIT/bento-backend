#!/usr/bin/python

import json
import requests
from datetime import datetime

def setapmrelease(project, tier, version, key):

   # get apm app ids
   API_ENDPOINT = 'https://api.newrelic.com/v2/applications.json'

   headers = {'Api-Key': key}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   
   for x in response.json()['applications']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       apm_id = x.get("id", "none")

   #set release for apm apps
   API_ENDPOINT = 'https://api.newrelic.com/v2/applications/{}/deployments.json'.format(apm_id)

   revision_name = '{}'.format(version)
   revision_description = '{} {} updated to v{}'.format(project, tier, version)
   #revision_time = datetime.utcnow()
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   data = {
     "deployment": {
       "revision": revision_name,
       "description": revision_description,
       #"timestamp": revision_time.isoformat(),
     }
   }

   try:
     response = requests.post(API_ENDPOINT, headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
   print('{} Created'.format(revision_name))