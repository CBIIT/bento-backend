import json
import re
import os
import boto3
import urllib3
import time


slack_url = os.getenv("SLACK_URL")
slack_channel = os.getenv("SLACK_CHANNEL")
epoch = time.ctime()

http = urllib3.PoolManager()
def handler(event, context):
    url = slack_url
    msg = {
        "channel": slack_channel,
        "icon_emoji": ":alert:",
        "attachments": [
              {
                  "fallback": "Errors from CloudFront",
                  "color": "#E01E5A",
                  "author_name": "@Bento Devops",
                  "title": "Bento CloudFront 4xx&5xx Errors",
                  "text": event['Records'][0]['Sns']['Message'],
                  "footer": "Slack API",
                  "mrkdwn_in": ["footer", "title"],
                  "footer": "bento devops",
                  "ts": epoch
              }
          ]
    }

    msg = json.dumps(msg).encode('utf-8')
    http.request('POST',url, body=msg)

