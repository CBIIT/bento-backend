#!/usr/bin/python

import os
import json
import requests
from monitors.alerts.conditions import set_nginx_conditions

def setnginxalertpolicy(project, tier, email_id, slack_id, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   policy_name = '{}-{}-nginx-policy'.format(project.lower(), tier.lower())
   policy_found = False
   headers = {'Api-Key': key}
   
   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['policies']:
     if policy_name in x.get("name", "none").lower():
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

     # add nginx conditions
     set_nginx_conditions.setnginxconditions(key, project, tier, policy_id)

     # add notification channels

     data = {
       "policy_id": '{}'.format(policy_id),
       "channel_ids": '{},{}'.format(email_id, slack_id)
     }

     try:
       response = requests.put('https://api.newrelic.com/v2/alerts_policy_channels.json', headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
     print("Policy {} created".format(policy_name))

   else:
     print("Policy {} already exists".format(policy_name))