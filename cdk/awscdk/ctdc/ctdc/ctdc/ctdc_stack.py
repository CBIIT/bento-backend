import os, sys
from aws_cdk import core
from aws_cdk import core as cdk
from aws_cdk import aws_ec2
from aws import iam, osCluster


class CTDCStack(cdk.Stack):
  def __init__(self, scope: cdk.Construct, ns: str, **kwargs) -> None:
    super().__init__(scope, ns, **kwargs)

    self.bentoVPC = aws_ec2.Vpc.from_lookup(self, "VPC", vpc_id = os.environ.get('VPC_ID'))
    
    #print(self.bentoVPC.vpc_arn)
    
    # IAM
    bentoIAM = iam.IAMResources.createResources(self, ns)
    
    # Opensearch
    bentoOSCluster = osCluster.OSCluster.createResources(self, ns)

    # Outputs
    cdk.CfnOutput(self, "Opensearch Endpoint",
        value=self.osDomain.domain_endpoint,
        description="The Opensearch Endpoint for this stack",
        export_name="osendpoint")