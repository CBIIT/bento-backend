from aws_cdk import core
from aws_cdk import aws_ec2 as ec2

class VPCPeering:
  def createResources(self, ns):

    # VPC Peering to Management VPC
    mgtVPC = ec2.Vpc.from_lookup(self, 'management-vpc',
        vpc_name='bento-management-vpc')
    
    vpc_peering = ec2.CfnVPCPeeringConnection(self, 'mgt-vpc-peering',
        peer_vpc_id=self.bentoVPC.vpc_id,
        vpc_id=mgtVPC.vpc_id)
    core.Tags.of(vpc_peering).add("Name", "{}-vpc-peering".format(ns))
    
    # Add peering routes from management VPC
    mgtPrivateSubnets = mgtVPC.select_subnets(subnet_type=ec2.SubnetType.PRIVATE)
    ec2.CfnRoute(self, 'mgt-vpc-peer-private',
        route_table_id=mgtPrivateSubnets.subnets[0].route_table.route_table_id,
        destination_cidr_block=self.bentoVPC.vpc_cidr_block,
        vpc_peering_connection_id=vpc_peering.ref )

    mgtPublicSubnets = mgtVPC.select_subnets(subnet_type=ec2.SubnetType.PUBLIC)
    ec2.CfnRoute(self, 'mgt-vpc-peer-public',
        route_table_id=mgtPublicSubnets.subnets[0].route_table.route_table_id,
        destination_cidr_block=self.bentoVPC.vpc_cidr_block,
        vpc_peering_connection_id=vpc_peering.ref )

    # Add peering routes from Bento VPC
    vpcPrivateSubnets = self.bentoVPC.select_subnets(subnet_type=ec2.SubnetType.PRIVATE)
    subnetNum = 1
    for subnet in vpcPrivateSubnets.subnets:
      ec2.CfnRoute(self, 'bento-vpc-peer-private-{}'.format(subnetNum),
          route_table_id=subnet.route_table.route_table_id,
          destination_cidr_block=mgtVPC.vpc_cidr_block,
          vpc_peering_connection_id=vpc_peering.ref )
      subnetNum += 1

    vpcPublicSubnets = self.bentoVPC.select_subnets(subnet_type=ec2.SubnetType.PUBLIC)
    subnetNum = 1
    for subnet in vpcPublicSubnets.subnets:
      ec2.CfnRoute(self, 'bento-vpc-peer-public-{}'.format(subnetNum),
          route_table_id=subnet.route_table.route_table_id,
          destination_cidr_block=mgtVPC.vpc_cidr_block,
          vpc_peering_connection_id=vpc_peering.ref )
      subnetNum += 1