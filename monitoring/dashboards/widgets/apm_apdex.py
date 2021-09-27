import json
import requests
import re

def apmApdex(project, tier, key):
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
          'visualization: { id: "viz.line" },'\
          'title: "APM Apdex Score",'\
          'layout: {'\
            'row: 1,'\
            'column: 9,'\
            'width: 4,'\
            'height: 3'\
          '},'\
          'rawConfiguration: {'\
            'nrqlQueries: ['\
              '{'\
                'accountId: 2292606,'\
                'query: "SELECT apdex(apm.service.apdex) as \'App server\', apdex(apm.service.apdex.user) as \'End user\' FROM Metric WHERE (entity.guid = \'' + apm_guid.group(1) + '\') TIMESERIES auto"'\
              '}'\
            ']'\
          '}'\
        '}'

     return(widgetContent)
   
   else:
     print('APM App not found:   ' + appName)