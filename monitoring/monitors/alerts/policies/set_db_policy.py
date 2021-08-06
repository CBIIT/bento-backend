#!/usr/bin/python

import os
import json
import requests
from monitors.alerts.conditions import set_disk_space_condition, set_memory_condition, set_cpu_condition

def setdbalertpolicy(project, tier, email_id, slack_id, synthetics_id, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   policy_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['policies']:
     if '{}-{}-db-policy'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       policy_found = True

   if not policy_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }
   
     data = {
       "policy": {
          "incident_preference": "PER_POLICY",
          "name": '{}-{}-db-policy'.format(project, tier)
       }
     }

     response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)
     policy_id = response.json()['policy'].get("id", "none")

     # add disk space condition
     set_disk_space_condition.setdiskspacecondition(key, '{}-aws-{}-neo4j'.format(project.lower(), tier.lower()), policy_id)
     
     # add memory condition
     set_memory_condition.setmemorycondition(key, '{}-aws-{}-neo4j'.format(project.lower(), tier.lower()), policy_id)
     
     # add cpu condition
     set_cpu_condition.setcpucondition(key, '{}-aws-{}-neo4j'.format(project.lower(), tier.lower()), policy_id)

     # add notification channels
     data = {
       "policy_id": '{}'.format(policy_id),
       "channel_ids": '{},{}'.format(email_id, slack_id)
     }

     response = requests.put('https://api.newrelic.com/v2/alerts_policy_channels.json', headers=headers, data=json.dumps(data), allow_redirects=False)
     print(response.text)

   else:
     print("Policy {}-{}-db-policy already exists".format(project, tier))