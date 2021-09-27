#!/usr/bin/python

import os
import json
import requests
from monitors.alerts.conditions import set_aws_redis_conditions

def setawsalertpolicy(project, tier, email_id, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   policy_name = '{}-{} AWS Policy'.format(project.title(), tier.title())
   policy_found = False
   headers = {'Api-Key': key}
   
   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['policies']:
     if policy_name in x.get("name", "none"):
       policy_found = True

   if not policy_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "policy": {
          "incident_preference": "PER_POLICY",
          "name": policy_name
       }
     }

     try:
       response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     policy_id = response.json()['policy'].get("id", "none")

     # add redis conditions
     set_aws_redis_conditions.setawsredisconditions(key, project, tier, policy_id)

     # add notification channels
     data = {
       "policy_id": '{}'.format(policy_id),
       "channel_ids": '{}'.format(email_id)
     }

     try:
       response = requests.put('https://api.newrelic.com/v2/alerts_policy_channels.json', headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     print('{} Created'.format(policy_name))

   else:
     print('{} already exists'.format(policy_name))