#!/usr/bin/env python
import sys, json
import imports.aws as aws

from constructs import Construct
from cdktf import App, TerraformStack
from configparser import ConfigParser
from getArgs import getArgs

class BentoStack(TerraformStack):
  def __init__(self, scope: Construct, ns: str):
    super().__init__(scope, ns)

    config = ConfigParser()
    config.read('bento.properties')
    
    bentoprovider = aws.AwsProvider(self, 'Aws', region=config[ns]['region'])

    ecsInstancePolicy = json.dumps(dict([["Action", "sts:AssumeRole"], ["Effect", "Allow"], ["Principal", dict([['Service', 'ec2.amazonaws.com']])]]))
    ecsInstanceRole = aws.IamRole(self, "ecs-instance-role", name="{}-ecs-instance-role".format(ns), assume_role_policy=ecsInstancePolicy)
    ecsInstanceRolePolicy = aws.IamRolePolicyAttachment(self, "ecs-instance-role-policy", role=ecsInstanceRole.name, policy_arn="arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role")
    ecsInstanceProfile = aws.IamInstanceProfile(self, "ecs-instance-profile", name="{}-ecs-instance-profile".format(ns), path="/", role=ecsInstanceRole.id)

    ecsServicePolicy = json.dumps(dict([["Action", "sts:AssumeRole"], ["Effect", "Allow"], ["Principal", dict([['Service', 'ecs.amazonaws.com']])]]))
    ecsServiceRole = aws.IamRole(self, "ecs-service-role", name="{}-ecs-service-role".format(ns), assume_role_policy=ecsServicePolicy)
    ecsServiceRolePolicy = aws.IamRolePolicyAttachment(self, "ecs-service-role-policy", role=ecsServiceRole.name, policy_arn="arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceRole")
    #ecsServiceProfile = aws.IamInstanceProfile(self, "ecs-instance-profile", name="{}-ecs-instance-profile".format(ns), path="/", role=ecsInstanceRole.id)
    

    helloInstance = aws.Instance(self, "hello", ami="ami-2757f631", instance_type=config[ns]['fronted_instance_type'])
    
    helloAlb = aws.Alb(self, "hello-test-lb")
    

    
    helloEcr = aws.EcrRepository(self, "hellorepo", name="hellorepo")
#    helloEcrPolicy = ECRActions.createECRPolicy(helloEcr)


if __name__=="__main__":
  tierName = getArgs.set_tier(sys.argv[1:])
  if not tierName:
    print('Please specify the tier to build:  awsApp.py -t <tier>')
    sys.exit(1)

  app = App()
  BentoStack(app, tierName)

  app.synth()