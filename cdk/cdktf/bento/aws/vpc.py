import imports.aws as aws

class VPCResources:
  def createResources(self, ns, config, bentoTags):

    # VPC
    bentoVPC = aws.Vpc(self, "bento-vpc", cidr_block=config[ns]['vpc_cidr_block'], tags=bentoTags)
    
    # Private Subnets
    for subnet in config[ns]['private_subnets'].split(","):
      aws.Subnet(self, '_' + subnet.replace('/', '_'), cidr_block=subnet, vpc_id=bentoVPC.id, tags=bentoTags)

    # Public Subnets
    for subnet in config[ns]['public_subnets'].split(","):
      aws.Subnet(self, '_' + subnet.replace('/', '_'), cidr_block=subnet, vpc_id=bentoVPC.id, tags=bentoTags)