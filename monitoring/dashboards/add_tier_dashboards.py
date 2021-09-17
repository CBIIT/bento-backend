#!/usr/bin/python

import sys, getopt
import set_overview

def main(argv):

   global project
   project = ''
   global tier
   tier = ''
   global key
   key = ''

   try:
      opts, args = getopt.getopt(argv,"hp:t:v:k:",["project=","tier=","key="])
   except getopt.GetoptError:
      print('add_tier_dashboards.py -p <project> -t <tier> -k <newrelic api key>')
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

if __name__ == "__main__":
   main(sys.argv[1:])
   
   print()
   print('Adding Overview Dashboard For: {} {}'.format(project, tier))
   print()

   set_overview.setoverviewdashboard(project, tier, key)