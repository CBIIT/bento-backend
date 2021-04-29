from aws_cdk import aws_ec2 as ec2

class VPCResources:
  def createResources(self, ns):

    # VPC
    self.bentoVPC = ec2.Vpc(self, "bento-vpc", cidr=self.config[ns]['vpc_cidr_block'])
    
    # Private Subnets
    privateSubnets = dict(zip(self.config[ns]['private_subnets'].split(","), self.config[ns]['availability_zones'].split(",")))
    for netID,zoneID, in privateSubnets.items():
      ec2.PrivateSubnet(self, 'private' + netID.replace('/', '_'), cidr_block=netID, availability_zone=zoneID, vpc_id=self.bentoVPC.vpc_id)

    # Public Subnets
    publicSubnets = dict(zip(self.config[ns]['public_subnets'].split(","), self.config[ns]['availability_zones'].split(",")))
    for netID,zoneID, in publicSubnets.items():
      ec2.PublicSubnet(self, 'public' + netID.replace('/', '_'), cidr_block=netID, availability_zone=zoneID, vpc_id=self.bentoVPC.vpc_id)