#!/usr/bin/python

import sys, getopt
import json
import requests

def main(argv):
   global project
   project = ''
   global tier
   tier = ''
   global key
   key = ''
   try:
      opts, args = getopt.getopt(argv,"hp:t:k:",["project=","tier=","key="])
   except getopt.GetoptError:
      print('monitor_query.py -p <project> -t <tier> -k <api key>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print('monitor_query.py -p <project> -t <tier> -k <api key>')
         sys.exit()
      elif opt in ("-p", "--project"):
         project = arg
      elif opt in ("-t", "--tier"):
         tier = arg
      elif opt in ("-k", "--key"):
         key = arg
   #print('Project is ', project)
   #print('Tier is ', tier)

def getsyntheticmonitors(project, tier):
   API_ENDPOINT = 'https://synthetics.newrelic.com/synthetics/api'

   headers = {'Api-Key': key}

   response = requests.get('{}/v3/monitors'.format(API_ENDPOINT), headers=headers)

   print('Synthetics Monitors:')
   print()
   for x in response.json()['monitors']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
   print()

def getalertchannels(project, tier):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_channels.json'

   headers = {'Api-Key': key}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   print('Alert Channels:')
   print()
   for x in response.json()['channels']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
   print()

def getalertconditions(policy):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['conditions']:
     print('    Alert Conditions:')
     for x in response.json()['conditions']:
       print('    - ' + x.get("name", "none"))
     print()

def getnrqlconditions(policy):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_nrql_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['nrql_conditions']:
     print('    Alert NRQL Conditions:')
     for x in response.json()['nrql_conditions']:
       print('    - ' + x.get("name", "none"))
     print()

def getexternalservices(policy):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_external_service_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['external_service_conditions']:
     print('    Alert NRQL Conditions:')
     for x in response.json()['external_service_conditions']:
       print('    - ' + x.get("name", "none"))
     print()

def getsyntheticsconditions(policy):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_synthetics_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['synthetics_conditions']:
     print('    Alert Synthetics Conditions:')
     for x in response.json()['synthetics_conditions']:
       print('    - ' + x.get("name", "none"))
     print()

def getpluginsconditions(policy):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_plugins_conditions.json'

   headers = {'Api-Key': key}
   data = {'policy_id': policy}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers, data=data)

   if response.json()['plugins_conditions']:
     print('    Alert Plugins Conditions:')
     for x in response.json()['plunigs_conditions']:
       print('    - ' + x.get("name", "none"))
     print()

def getalertpolicies(project, tier):
   API_ENDPOINT = 'https://api.newrelic.com/v2/alerts_policies.json'

   headers = {'Api-Key': key}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   print('Alert Policies:')
   print()
   for x in response.json()['policies']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
       getalertconditions(x.get("id", "none"))
       getnrqlconditions(x.get("id", "none"))
       getexternalservices(x.get("id", "none"))
       getsyntheticsconditions(x.get("id", "none"))
       getpluginsconditions(x.get("id", "none"))
       print()

def getapmapps(project, tier):
   API_ENDPOINT = 'https://api.newrelic.com/v2/applications.json'

   headers = {'Api-Key': key}

   response = requests.get('{}'.format(API_ENDPOINT), headers=headers)

   print('APM Applications:')
   print()
   for x in response.json()['applications']:
     if project.lower() in x.get("name", "none").lower() and tier.lower() in x.get("name", "none").lower():
       print('  ' + x.get("name", "none"))
   print()

if __name__ == "__main__":
   main(sys.argv[1:])
   getsyntheticmonitors(project, tier)
   getalertchannels(project, tier)
   getalertpolicies(project, tier)
   getapmapps(project, tier)