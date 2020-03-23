import requests
from requests.exceptions import ConnectionError
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--servers', nargs='*')
parser.add_argument('--slackURL')

slackHeaders = {'content-type': 'application/json', 'Accept-Charset': 'UTF-8'}
args = parser.parse_args()
slack_url = args.slackURL

# POST function to send to Slack
def post_message_to_slack(message_text):
    return requests.post(slack_url, data=message_text, headers=slackHeaders)

for i in args.servers:
    graphURL = 'http://' + i + '.nci.nih.gov:7474/graphql/'

    try:
        request = requests.get(graphURL)
    except ConnectionError:
        print('The graph QL endpoint on ' + i + ' is NOT responding - please verify that this application is working')
        slack_message = 'The graph QL endpoint on {} is NOT responding - please verify that this application is working'.format(i)
        payload = '{"text":"' + slack_message + '"}'
        post_message_to_slack(payload)

    else:
        print('The graph QL endpoint on ' + i + ' is available')
~
