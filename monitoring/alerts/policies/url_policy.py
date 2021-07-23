#!/usr/bin/python

import os
import json
import requests

def setalertpolicy(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   policy_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['policies']:
     if '{}-{}-url-policy'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       policy_found = True

   if not policy_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "policy": {
          "incident_preference": "PER_POLICY",
          "name": '{}-{}-url-policy'.format(project, tier)
       }
     }

     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
   else:
     print("Channel {}-{}-url-policy already exists".format(project, tier))