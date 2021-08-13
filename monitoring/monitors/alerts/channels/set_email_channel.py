#!/usr/bin/python

import os
import json
import requests

def setalertemail(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_channels.json'
   DEVOPS_EMAIL = os.getenv('EMAIL')

   channel_name = '{}-{}-email-alerts'.format(project.lower(), tier.lower())
   channel_found = False
   headers = {'Api-Key': key}
   
   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['channels']:
     if channel_name in x.get("name", "none").lower():
       channel_found = True
       channel_id = x.get('id')

   if not channel_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "channel": {
          "name": channel_name,
          "type": "Email",
          "configuration": {
             "recipients": DEVOPS_EMAIL,
             "include_json_attachment": True
          }
       }
     }

     try:
       response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     print("Channel {} created".format(channel_name))
     channel_id = response.json()['channels'][0].get('id')
   else:
     print("Channel {} already exists".format(channel_name))
     
   return(channel_id)