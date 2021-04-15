#!/usr/bin/env python
import sys, json

from aws_cdk import core as cdk
from configparser import ConfigParser
from getArgs import getArgs
#from aws import iam, ec2
from aws import iam

class BentoStack(cdk.Stack):
  def __init__(self, scope: cdk.Construct, ns: str, **kwargs) -> None:
    super().__init__(scope, ns, **kwargs)

    config = ConfigParser()
    config.read('bento.properties')
    
    bentoTags = json.loads(config[ns]['tags'])

    # IAM
    bentoIAM = iam.IAMResources.createResources(self, ns, bentoTags)
    
    # EC2
    #bentoEC2 = ec2.EC2Resources.createResources(self, ns, config, bentoTags, bentoIAM)


if __name__=="__main__":
  tierName = getArgs.set_tier(sys.argv[1:])
  if not tierName:
    print('Please specify the tier to build:  awsApp.py -t <tier>')
    sys.exit(1)

  app = cdk.App()
  BentoStack(app, tierName)

  app.synth()