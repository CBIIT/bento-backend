#!/usr/bin/python

import sys, getopt
from alerts.channels import email_channel, slack_channel
from alerts.policies import url_policy

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
   print('Adding Monitor Configuration For: {} {}'.format(project, tier))
   print()
   
   email_channel.setalertemail(project, tier, key)
   slack_channel.setalertslack(project, tier, key)
   url_policy.setalertpolicy(project, tier, key)