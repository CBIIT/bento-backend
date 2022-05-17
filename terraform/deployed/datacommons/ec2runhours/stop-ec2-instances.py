import boto3
import logging


logger = logging.getLogger()
logger.setLevel(logging.INFO)
region = "us-east-1"
ec2 = boto3.resource('ec2', region_name=region)


def handler(event, context):
    filters = [
        {
            'Name': 'tag:ShutdownInstance',
            'Values': ['Yes']
        },
        {
            'Name': 'instance-state-name',
            'Values': ['running']
        }
    ]

    instances = ec2.instances.filter(Filters=filters)

    running_instances = [instance.id for instance in instances]

    if len(running_instances) > 0:
        stop_instance = ec2.instances.filter(InstanceIds=running_instances).stop()