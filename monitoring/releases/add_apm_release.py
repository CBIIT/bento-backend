#!/usr/bin/python

import sys, getopt
import set_release

def main(argv):

   global project
   project = ''
   global tier
   tier = ''
   global version
   version = ''
   global key
   key = ''

   try:
      opts, args = getopt.getopt(argv,"hp:t:v:k:",["project=","tier=","version=","key="])
   except getopt.GetoptError:
      print('monitor_query.py -p <project> -t <tier> -v <version> -k <newrelic api key>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print('monitor_query.py -p <project> -t <tier> -v <version> -k <api key>')
         sys.exit()
      elif opt in ("-p", "--project"):
         project = arg
      elif opt in ("-t", "--tier"):
         tier = arg
      elif opt in ("-v", "--version"):
         version = arg
      elif opt in ("-k", "--key"):
         key = arg

if __name__ == "__main__":
   main(sys.argv[1:])
   
   print()
   print('Adding APM Release For: {} {} {}'.format(project, tier, version))
   print()

   set_release.setapmrelease(project, tier, version, key)