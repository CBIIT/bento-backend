import json
import os
import urllib3
import time

slack_url = os.getenv("SLACK_URL")
slack_channel = os.getenv("SLACK_CHANNEL")
epoch_time = int(time.time())

http = urllib3.PoolManager()


def get_blocked_ips(blocked_ips):
    fields = []
    for ip in blocked_ips:
        field = {
            "title": "IP",
            "value": ip,
            "short": False
        }

        fields.append(field)
    return fields


def send_blocked_ips_report(blocked_ips):
    url = slack_url
    msg = {
        "channel": slack_channel,
        "attachments": [
            {
                "fallback": "Blocked IP report from WAFv2",
                "color": "#E01E5A",
                "author_name": "@Bento Devops",
                "title": "Blocked IP report from WAFv2",
                "fields": get_blocked_ips(blocked_ips),
                "mrkdwn_in": ["footer", "title"],
                "footer": "bento devops",
                "ts": epoch_time,
                "icon_emoji": ":alert:",
            }
        ]
    }
    msg = json.dumps(msg).encode('utf-8')
    http.request('POST', url, body=msg)
