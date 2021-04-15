#!/usr/bin/env python
import sys, json
#import imports.aws as aws

from imports.aws import AwsProvider
from constructs import Construct
from cdktf import App, TerraformStack
from configparser import ConfigParser
from getArgs import getArgs
from aws import iam, ec2

class BentoStack(TerraformStack):
  def __init__(self, scope: Construct, ns: str):
    super().__init__(scope, ns)

    config = ConfigParser()
    config.read('bento.properties')
    
    #bentoProvider = aws.AwsProvider(self, 'Aws', region=config[ns]['region'], profile=config[ns]['profile'], shared_credentials_file="/bento/creds/credentials")
    bentoProvider = AwsProvider(self, 'Aws', region=config[ns]['region'], profile=config[ns]['profile'], shared_credentials_file="/bento/creds/credentials")
    bentoTags = json.loads(config[ns]['tags'])

    # IAM
    bentoIAM = iam.IAMResources.createResources(self, ns, bentoTags)
    
    # EC2
    bentoEC2 = ec2.EC2Resources.createResources(self, ns, config, bentoTags, bentoIAM)


if __name__=="__main__":
  tierName = getArgs.set_tier(sys.argv[1:])
  if not tierName:
    print('Please specify the tier to build:  awsApp.py -t <tier>')
    sys.exit(1)

  app = App()
  BentoStack(app, tierName)

  app.synth()