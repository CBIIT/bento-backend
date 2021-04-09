#!/usr/bin/env python
import sys

from constructs import Construct
from cdktf import App, TerraformStack
from configparser import ConfigParser
from getArgs import getArgs

from imports.aws import Instance, AwsProvider

from aws.ec2 import EC2Actions
from aws.alb import ALBActions
from aws.ecr import ECRActions
from aws.iam import IAMActions


class MyStack(TerraformStack):
  def __init__(self, scope: Construct, ns: str):
    super().__init__(scope, ns)

    config = ConfigParser()
    config.read('bento.properties')
    
    AwsProvider(self, 'Aws', region=config[ns]['region'])
    helloInstance = EC2Actions.createInstance(self, config, ns)
    helloAlb = ALBActions.createALB(self)
    helloRole = IAMActions.createRole(self)
    helloEcr = ECRActions.createECR(self)
#    helloEcrPolicy = ECRActions.createECRPolicy(helloEcr)


if __name__=="__main__":
  tierName = getArgs.set_tier(sys.argv[1:])
  if not tierName:
    print('Please specify the tier to build:  awsApp.py -t <tier>')
    sys.exit(1)

  app = App()
  MyStack(app, tierName)

  app.synth()