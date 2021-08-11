#!/usr/bin/python

import os
import json
import requests
from monitors.alerts.conditions import set_aws_redis_conditions

def setawsalertpolicy(project, tier, email_id, slack_id, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   policy_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['policies']:
     if '{}-{}-aws-policy'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       policy_found = True

   if not policy_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "policy": {
          "incident_preference": "PER_POLICY",
          "name": '{}-{}-aws-policy'.format(project, tier)
       }
     }

     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
     policy_id = response.json()['policy'].get("id", "none")

     # add redis conditions
     set_aws_redis_conditions.setawsredisconditions(key, project, tier, policy_id)

     # add notification channels

     data = {
       "policy_id": '{}'.format(policy_id),
       "channel_ids": '{},{}'.format(email_id, slack_id)
     }

     response = requests.put('https://api.newrelic.com/v2/alerts_policy_channels.json', headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
     print("Policy {}-{}-aws-policy created".format(project, tier))

   else:
     print("Policy {}-{}-aws-policy already exists".format(project, tier))