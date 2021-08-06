#!/usr/bin/python

import os
import json
import requests

def seturlmonitor(project, tier, key):
   API_ENDPOINT = 'https://synthetics.newrelic.com/synthetics/api/v3/monitors'
   DOMAIN = os.getenv('URL_DOMAIN')

   if tier.lower() == 'prod':
     freq = 10
   else:
     freq = 30

   # set monitor configuration
   data = {
       "name": '{}-{}-url-monitor'.format(project, tier),
       "type": "BROWSER",
       "frequency": freq,
       "uri": 'https://{}-{}.{}'.format(project, tier, DOMAIN),
       "locations": [ "AWS_US_EAST_1" ],
       "status": "ENABLED",
       "slaThreshold": 7.0,
   }
   
   monitor_found = False
   headers = {'Api-Key': key}
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['monitors']:
     if '{}-{}-url-monitor'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       monitor_found = True

   if not monitor_found:
     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }

     requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)

   else:
     print("Monitor {}-{}-url-monitor already exists - updating with current configuration".format(project, tier))

     headers = {
         "Api-Key": key,
         "Content-Type": "application/json"
     }

     requests.put('{}/{}'.format(API_ENDPOINT, x.get("id", "none")), headers=headers, data=json.dumps(data), allow_redirects=False)

   # get the newly created monitor
   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   for x in response.json()['monitors']:
     if '{}-{}-url-monitor'.format(project.lower(), tier.lower()) in x.get("name", "none").lower():
       url_monitor = x

   return(url_monitor.get('id'))