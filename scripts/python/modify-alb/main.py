# This is a sample Python script.

# Press ⌃R to execute it or replace it with your code.
# Press Double ⇧ to search everywhere for classes, files, tool windows, actions, and settings.
import boto3
from botocore.config import Config

config = Config(
    region_name="us-east-1"
)
session = boto3.Session(profile_name="icdc")
client = session.client('elbv2', config=config)

response = client.describe_load_balancers()
albs = [{"alb_arn": alb["LoadBalancerArn"], "alb_name": alb["LoadBalancerName"]} for alb in response["LoadBalancers"]]

for alb in albs:
    response = client.modify_load_balancer_attributes(
        LoadBalancerArn=alb["alb_arn"],
        Attributes=[
            {
                'Key': 'access_logs.s3.enabled',
                'Value': 'true'
            },
            {
                'Key': 'access_logs.s3.bucket',
                'Value': 'bento-alb-access-logs'
            },
            {
                'Key': 'access_logs.s3.prefix',
                'Value': alb["alb_name"]
            },
        ]
    )
