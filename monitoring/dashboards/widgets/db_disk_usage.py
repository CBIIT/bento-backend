def dbDiskUsage(project, tier):
   # Disk Usage Stats
   widgetContent = '{'\
          'visualization: { id: "viz.area" },'\
          'title: "Database Instance Disk Usage (%)",'\
          'layout: {'\
            'row: 7,'\
            'column: 9,'\
            'width: 4,'\
            'height: 3'\
          '},'\
          'rawConfiguration: {'\
            'nrqlQueries: [{'\
              'accountId: 2292606,'\
              'query: "SELECT average(diskUsedPercent) as \'Storage used %\' FROM StorageSample WHERE label.Name IN (\'' + project + '-' + tier + '-neo4j-4\') TIMESERIES auto"'\
            '}]'\
          '}'\
        '}'

   return(widgetContent)