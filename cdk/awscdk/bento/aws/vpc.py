from aws_cdk import core
from aws_cdk import aws_ec2 as ec2

class VPCResources:
  def createResources(self, ns):

    # VPC
    self.bentoVPC = ec2.Vpc(self, "bento-vpc",
        cidr=self.config[ns]['vpc_cidr_block'])
    
    # VPC Peering to Management VPC
    mgtVPC = ec2.Vpc.from_lookup(self, 'management-vpc',
        vpc_name='bento-management-vpc')
    
    vpc_peering = ec2.CfnVPCPeeringConnection(self, 'mgt-vpc-peering',
        peer_vpc_id=self.bentoVPC.vpc_id,
        vpc_id=mgtVPC.vpc_id)
    core.Tags.of(vpc_peering).add("Name", "{}-vpc-peering".format(ns))
    
    # Add peering routes
    #vpcSubnets = self.bentoVPC.select_subnets(subnet_type=ec2.SubnetType.PRIVATE)
    
    #subnetNum = 1
    #for subnet in vpcSubnets.subnets:
    #  ec2.CfnRoute(self, 'vpc-peer-{}'.format(subnetNum),
    #      route_table_id=subnet.route_table.route_table_id,
    #      destination_cidr_block=self.bentoVPC.vpc_cidr_block,
    #      vpc_peering_connection_id=vpc_peering.ref )
    #  subnetNum += 1