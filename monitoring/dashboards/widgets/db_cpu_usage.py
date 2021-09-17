def dbCPUUsage(project, tier):
   # CPU Usage Stats
   widgetContent = '{'\
          'visualization: { id: "viz.line" },'\
          'title: "Database CPU Usage (%)",'\
          'layout: {'\
            'row: 7,'\
            'column: 1,'\
            'width: 4,'\
            'height: 3'\
          '},'\
          'rawConfiguration: {'\
            'nrqlQueries: [{'\
              'accountId: 2292606,'\
              'query: "SELECT average(cpuSystemPercent) AS \'System\', average(cpuIOWaitPercent) AS \'I/O wait\', average(cpuUserPercent) AS \'User\', average(cpuStealPercent) AS \'Steal\' FROM SystemSample WHERE label.Name IN (\'' + project + '-' + tier + '-neo4j-4\') TIMESERIES auto"'\
            '}]'\
          '}'\
        '}'

   return(widgetContent)