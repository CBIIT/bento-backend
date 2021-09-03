#!/usr/bin/python

import os
import json
import requests
import re
from tags import set_tags_nrql

def seturlmonitor(project, tier, key):
   API_ENDPOINT = 'https://synthetics.newrelic.com/synthetics/api/v3/monitors'
   DOMAIN = os.getenv('URL_DOMAIN')

   if tier.lower() == 'prod':
     freq = 10
     monitor_uri = 'https://{}'.format(DOMAIN)
   else:
     freq = 30
     monitor_uri = 'https://{}-{}.{}'.format(project, tier, DOMAIN)

   # set monitor configuration
   monitor_name = '{}-{} Url Monitor'.format(project.title(), tier.title())
   data = {
       "name": monitor_name,
       "type": "BROWSER",
       "frequency": freq,
       "uri": monitor_uri,
       "locations": [ "AWS_US_EAST_1" ],
       "status": "ENABLED",
       "slaThreshold": 7.0,
   }
   
   monitor_found = False
   headers = {'Api-Key': key}
   
   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['monitors']:
     if monitor_name in x.get("name", "none"):
       print('{} already exists - updating with current configuration'.format(monitor_name))

       headers = {
           "Api-Key": key,
           "Content-Type": "application/json"
       }

       try:
         requests.put('{}/{}'.format(API_ENDPOINT, x.get("id", "none")), headers=headers, data=json.dumps(data), allow_redirects=False)
       except requests.exceptions.RequestException as e:
         raise SystemExit(e)

     else:
       headers = {
           "Api-Key": key,
           "Content-Type": "application/json"
       }

       try:
         response = requests.post('{}'.format(API_ENDPOINT), headers=headers, data=json.dumps(data), allow_redirects=False)
       except requests.exceptions.RequestException as e:
         raise SystemExit(e)
     
   # get the newly created monitor
   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   for x in response.json()['monitors']:
     if monitor_name in x.get("name", "none"):
       url_monitor = x

   # set tags on the monitor
   set_tags_nrql.settagsnrql(project, tier, url_monitor.get('name'), key)

   return(url_monitor.get('id'))