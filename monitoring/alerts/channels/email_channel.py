#!/usr/bin/python

import os
import json
import requests

def setalertemail(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_channels.json'
   DEVOPS_EMAIL = os.getenv('EMAIL')

   channel_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['channels']:
     if '{}-{}-email-alerts'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       channel_found = True

   if not channel_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "channel": {
          "name": '{}-{}-email-alerts'.format(project, tier),
          "type": "Email",
          "configuration": {
             "recipients": DEVOPS_EMAIL,
             "include_json_attachment": True
          }
       }
     }

     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
   else:
     print("Channel {}-{}-email-alerts already exists".format(project, tier))