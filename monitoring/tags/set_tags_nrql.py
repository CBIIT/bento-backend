#!/usr/bin/python

import json
import requests
import re

def settagsnrql(project, tier, entity, key):
   API_ENDPOINT = 'https://api.newrelic.com/graphql'
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }

   # set tags
   data = {"query":"{\n  actor {\n    entitySearch(query: \"name = \'" + entity + "\'\") {\n      query\n      results {\n        entities {\n          guid\n        }\n      }\n    }\n  }\n}\n", "variables":""}
   
   try:
     response = requests.post(API_ENDPOINT, headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)
         
   guid = re.findall(r'^.*?\bguid\b\":\"([^$]*?)\"',response.text)[0]
   
   tagdefs = {
       'key: "Environment", values: "{}"'.format(tier),
       'key: "Project", values: "{}"'.format(project)
   }
   
   for tag in tagdefs:
     data = {"query":"mutation {\n  taggingAddTagsToEntity(guid: \"" + guid + "\", tags: { " + tag + " }) {\n    errors {\n      message\n    }\n  }\n}\n", "variables":""}
     
     try:
       response = requests.post(API_ENDPOINT, headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)
         
   print('Added tags to {}'.format(entity))