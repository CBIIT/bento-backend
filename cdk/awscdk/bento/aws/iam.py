import json
from aws_cdk import aws_iam as iam

class IAMResources:
  def createResources(self, ns, bentoTags):

    # IAM
    # ECS Instance
    #ecsInstancePolicyDoc = {
    #  "Version": "2012-10-17",
    #  "Statement": [
    #    {
    #      "Action": [ "sts:AssumeRole" ],
    #      "Effect": "Allow",
    #      "Principal": {
    #        "Service": [
    #          "ec2.amazonaws.com"
    #        ]
    #      }
    #    }
    #  ]
    #}
    
    self.ecsInstanceRole = iam.Role(self, "ecs-instance-role", role_name="{}-ecs-instance-role".format(ns), assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"))
    self.ecsInstanceRole.add_managed_policy(iam.ManagedPolicy.from_aws_managed_policy_name("service-role/AmazonEC2ContainerServiceforEC2Role"))