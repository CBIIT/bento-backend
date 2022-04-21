import boto3
import logging


logger = logging.getLogger()
logger.setLevel(logging.INFO)
region = "us-east-1"
ecs = boto3.client('ecs')


def lambda_handler(event, context):

    if event['detail']['state'] == 'stop':
        count = 0
    elif event['detail']['state'] == 'start':
        count = 1
    else:
        sys.exit('ERROR: Invalid Status Set')
            
    
    clusters = ecs.list_clusters()['clusterArns']

    for c in clusters:
        svc = ecs.list_services(cluster=c, launchType='FARGATE')['serviceArns']
        for s in svc:
            tags = ecs.list_tags_for_resource(resourceArn=s)['tags']
            for t in tags:
                if ('key', 'ScaleService') in t.items() and ('value', 'yes') in t.items():
                    response = ecs.update_service(cluster=c, service=s, desiredCount=count)