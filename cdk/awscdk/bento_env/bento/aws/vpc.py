import os
from aws_cdk import aws_ec2 as ec2

class VPCResources:
  def createResources(self, ns):

    # VPC
    self.bentoVPC = ec2.Vpc(self,
        "{}-vpc".format(ns),
        max_azs=2,
        cidr=os.environ.get('VPC_CIDR_BLOCK'))