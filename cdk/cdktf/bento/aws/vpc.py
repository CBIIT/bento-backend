import imports.aws as aws

class VPCResources:
  def createResources(self, ns, config, bentoTags):

    # VPC
    bentoVPC = aws.Vpc(self, "bento-vpc", ami="ami-2757f631", instance_type=config[ns]['fronted_instance_type'], iam_instance_profile=self.ecsInstanceProfile.name, tags=bentoTags)
    
    
    module "vpc" {
  source = "../../../modules/networks/vpc"
  stack_name = var.stack_name
  env = var.env
  availability_zones = var.availability_zones
  private_subnets = var.private_subnets
  public_subnets = var.public_subnets
  vpc_cidr_block = var.vpc_cidr_block
  tags = var.tags
}