import json
import requests
import re

def apmErrorGraph(project, tier, key):
   API_ENDPOINT = 'https://api.newrelic.com/graphql'
   headers = {
       "Api-Key": key,
       "Content-Type": "application/json"
   }
   
   # search for APM app
   if project.lower() == 'bento':
     appName = project + '-aws-' + tier + '-backend'
   else:
     appName = project + '-cloudone-' + tier + '-backend'
     
   data = {'query':'{actor {'
     'entitySearch(query: "name = \'' + appName + '\' AND domain = \'APM\'") {'
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

   apm_guid = re.search('"guid":"(.*)"', response.text)
   if apm_guid:
     #apm_guid.group(1)

     widgetContent = '{'\
          'visualization: { id: "viz.area" },'\
          'title: "APM Errors",'\
          'layout: {'\
            'row: 1,'\
            'column: 5,'\
            'width: 4,'\
            'height: 3'\
          '},'\
          'rawConfiguration: {'\
            'nrqlQueries: ['\
              '{'\
                'accountId: 2292606,'\
                'query: "SELECT count(apm.service.error.count) / count(apm.service.transaction.duration) as \'Web errors\' FROM Metric WHERE (entity.guid = \'' + apm_guid.group(1) + '\') AND (transactionType = \'Web\')  SINCE 1800 seconds AGO TIMESERIES"'\
              '},'\
              '{'\
                'accountId: 2292606,'\
                'query: "SELECT count(apm.service.error.count) / count(apm.service.transaction.duration) as \'All errors\' FROM Metric WHERE (entity.guid = \'' + apm_guid.group(1) + '\')  SINCE 1800 seconds AGO TIMESERIES"'\
              '}'\
            ']'\
          '}'\
        '}'

     return(widgetContent)
   
   else:
     print('APM App not found:   ' + appName)