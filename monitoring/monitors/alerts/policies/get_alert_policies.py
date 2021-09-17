#!/usr/bin/python

import json
import requests
from monitors.alerts.conditions import get_alert_conditions, get_nrql_conditions, get_external_services, get_synthetics_conditions

def getalertpolicies(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   headers = {'Api-Key': key}

   try:
     response = requests.get('{}'.format(API_ENDPOINT), headers=headers)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   print('Alert Policies:')
   for x in response.json()['policies']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
       get_alert_conditions.getalertconditions(x.get("id", "none"), key)
       get_nrql_conditions.getnrqlconditions(x.get("id", "none"), key)
       get_external_services.getexternalservices(x.get("id", "none"), key)
       get_synthetics_conditions.getsyntheticsconditions(x.get("id", "none"), key)
       print()