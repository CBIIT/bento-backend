def dbMemUsage(project, tier):
   # CPU Usage Stats
   widgetContent = '{'\
          'visualization: { id: "viz.area" },'\
          'title: "Database Memory Usage (%)",'\
          'layout: {'\
            'row: 7,'\
            'column: 5,'\
            'width: 4,'\
            'height: 3'\
          '},'\
          'rawConfiguration: {'\
            'nrqlQueries: [{'\
              'accountId: 2292606,'\
              'query: "SELECT average(memoryUsedPercent) AS \'Memory used %\' FROM SystemSample WHERE label.Name IN (\'' + project + '-' + tier + '-neo4j-4\') TIMESERIES auto"'\
            '}]'\
          '}'\
        '}'

   return(widgetContent)