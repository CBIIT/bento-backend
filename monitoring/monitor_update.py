#!/usr/bin/python

import sys, getopt
from monitors.alerts.channels import set_email_channel, set_slack_channel
from monitors.alerts.policies import set_url_policy, set_apm_policy, set_db_policy, set_aws_policy, set_redis_policy, set_nginx_policy
from monitors.synthetics import set_url_monitor

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

if __name__ == "__main__":
   main(sys.argv[1:])
   
   print()
   print('Adding Monitor Configuration For: {} {}'.format(project, tier))
   print()
   
   email_id = set_email_channel.setalertemail(project, tier, key)
   slack_id = set_slack_channel.setalertslack(project, tier, key)
   synthetics_id = set_url_monitor.seturlmonitor(project, tier, key)
   set_url_policy.seturlalertpolicy(project, tier, email_id, slack_id, synthetics_id, key)
   set_apm_policy.setapmalertpolicy(project, tier, email_id, slack_id, key)
   set_db_policy.setdbalertpolicy(project, tier, email_id, synthetics_id, key)
   set_nginx_policy.setnginxalertpolicy(project, tier, email_id, key)
   
   if project.lower() == 'bento':
     set_aws_policy.setawsalertpolicy(project, tier, email_id, key)
   else:
     set_redis_policy.setredisalertpolicy(project, tier, email_id, key)