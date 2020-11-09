#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "Bento"
  Environment = "dev"
}
#specify vpc cidr 
vpc_cidr_block = "172.17.0.0/16"

#define private subnet to use
private_subnets = ["172.17.10.0/24","172.17.11.0/24"]

#define public subnets to use. Note you must specify at least two subnets
public_subnets = ["172.17.0.0/24","172.17.1.0/24"]

#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "bento"

#specify availability zones to provision your resources. Note the availability zone must match the number of public subnets. Also availability zones depends on the region.
#If you change the region use the corresponding availability zones
availability_zones = ["us-east-1b","us-east-1c"]

env = "dev"