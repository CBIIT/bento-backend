#!/usr/bin/python

import json
import requests
import re
from widgets.markdown import markdown_test
from widgets.apm_errors import apmErrorGraph
from widgets.apm_apdex import apmApdex
from widgets.db_cpu_usage import dbCPUUsage
from widgets.db_mem_usage import dbMemUsage
from widgets.db_disk_usage import dbDiskUsage

def setoverviewdashboard(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/graphql'
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }

   def getDashboard():
     # search for existing dashboards with the same name   
     data = {'query':'{actor {'
       'entitySearch(query: "name = \'' + project.title() + ' Overview: ' + tier.lower() + '\' AND type IN (\'DASHBOARD\')") {'
         'results {'
           'entities {'
             'guid,'
           '}'
         '}'
       '}'
     '}}'}

     try:
       response = requests.post(API_ENDPOINT, headers=headers, data=json.dumps(data), allow_redirects=False)
     except requests.exceptions.RequestException as e:
       raise SystemExit(e)

     return(re.search('"guid":"(.*)"', response.text))

   # Set Dashboard Data - this is the data that will be added to the overview dashboard
   # markdown widget
   markdownWidget = markdown_test(project, tier)

   # APM errors
   apmErrorWidget = apmErrorGraph(project, tier, key)
   # APM apdex
   apmApdexWidget = apmApdex(project, tier, key)

   # DB CPU Usage
   dbCPUUsageWidget = dbCPUUsage(project, tier)
   # DB Memeory Usage
   dbMemUsageWidget = dbMemUsage(project, tier)
   # DB Disk Usage
   dbDiskUsageWidget = dbDiskUsage(project, tier)

   dash_data = ', dashboard: {'\
     'name: "' + project.title() + ' Overview: ' + tier.lower() + '",'\
     'permissions: PUBLIC_READ_ONLY,'\
     'pages: {'\
       'name: "page_1",'\
       'widgets: ['\
         '' + markdownWidget + ','\
         '' + apmErrorWidget + ','\
         '' + apmApdexWidget + ','\
         '' + dbCPUUsageWidget + ','\
         '' + dbMemUsageWidget + ','\
         '' + dbDiskUsageWidget + ''\
       ']'\
       '}'\
     '}){'\
       'errors {'\
         'description,'\
         'type'\
       '}'\
     '}'\
   '}'

   dash_guid = getDashboard()

   # set the query type for update or create
   if dash_guid:
     queryType = 'dashboardUpdate(guid: "' + dash_guid.group(1) + '"'
     pageGuid = dash_guid.group(1)
   else:
     queryType = 'dashboardCreate(accountId: 2292606'

   data = {'query':'mutation {' + queryType + dash_data}

   try:
     response = requests.post(API_ENDPOINT, headers=headers, data=json.dumps(data), allow_redirects=False)
   except requests.exceptions.RequestException as e:
     raise SystemExit(e)

   print('Dashboard added:  {} {} Overview'.format(project, tier))