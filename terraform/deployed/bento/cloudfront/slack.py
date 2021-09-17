import json
import re
import os
import boto3
import urllib3
import time


slack_url = os.getenv("SLACK_URL")
slack_channel = os.getenv("SLACK_CHANNEL")
epoch = time.ctime()
slack_colors =  {
    "ALARM": "#E01E5A",
    "OK": "#2EB67D"
}

http = urllib3.PoolManager()
def handler(event, context):
    url = slack_url

    alarm_type = event['Records'][0]['Sns']['Message']['NewStateValue']
    alarm_changed = event['Records'][0]['Sns']['Message']['StateChangeTime']
    alarm_new_state = event['Records'][0]['Sns']['Message']['NewStateReason']
    alarm_name = event['Records'][0]['Sns']['Message']['AlarmName']

    msg = {
        "channel": slack_channel,
        "icon_emoji": ":alert:",
        "attachments": [
              {
                  "fallback": "Errors from CloudFront",
                  "color": slack_colors[alarm_type],
                  "author_name": "@Bento Devops",
                  "title": "Bento CloudFront 4xx&5xx Errors",
                  "text": f'''{alarm_name} has changed state to {alarm_type} because\n {alarm_new_state} \non {alarm_changed}''',
                  "footer": "Slack API",
                  "mrkdwn_in": ["footer", "title"],
                  "footer": "bento devops",
                  "ts": epoch
              }
          ]
    }

    msg = json.dumps(msg).encode('utf-8')
    http.request('POST',url, body=msg)

