#!/usr/bin/python

import sys, getopt
import json
import requests
import subprocess
from monitors.synthetics import get_url_monitor
from monitors.alerts.channels import get_alert_channels
from monitors.alerts.policies import get_alert_policies
from monitors.apm import get_apm_apps
from monitors.sumologic.collectors import get_sumo_collectors

def main(argv):
   global project
   project = ''
   global tier
   tier = ''
   global key
   key = ''
   global auth
   auth = ''
   try:
      opts, args = getopt.getopt(argv,"hp:t:k:a:",["project=","tier=","key=","auth="])
   except getopt.GetoptError:
      print('monitor_query.py -p <project> -t <tier> -k <newrelic api key> -a <sumologic basic auth>')
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
      elif opt in ("-a", "--auth"):
         auth = arg
   #print('Project is ', project)
   #print('Tier is ', tier)

if __name__ == "__main__":
   main(sys.argv[1:])
   
   print()
   print('Monitor Information for: {} {}'.format(project, tier))
   print()
   
   get_url_monitor.geturlmonitor(project, tier, key)
   get_alert_channels.getalertchannels(project, tier, key)
   get_alert_policies.getalertpolicies(project, tier, key)
   get_apm_apps.getapmapps(project, tier, key)
   get_sumo_collectors.getsumocollectors(project, tier, auth)