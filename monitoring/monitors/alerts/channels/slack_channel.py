#!/usr/bin/python

import os
import json
import requests

def setalertslack(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_channels.json'
   DEVOPS_SLACK_URL = os.getenv('SLACK_URL')
   DEVOPS_SLACK_CHANNEL = os.getenv('SLACK_CHANNEL')

   channel_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['channels']:
     if '{}-{}-slack-alerts'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       channel_found = True
       channel_id = x.get('id')

   if not channel_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "channel": {
          "name": '{}-{}-slack-alerts'.format(project, tier),
          "type": "Slack",
          "configuration": {
               "url": DEVOPS_SLACK_URL,
               "channel": DEVOPS_SLACK_CHANNEL
          }
       }
     }

     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
     channel_id = response.json()['channels'][0].get('id')
   else:
     print("Channel {}-{}-slack-alerts already exists".format(project, tier))
     
   return(channel_id)