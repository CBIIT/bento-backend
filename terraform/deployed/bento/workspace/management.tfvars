#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "Bento"
  Environment = "Management"
  Region = "us-east-1"
}
#specify vpc cidr 
mgt_vpc_cidr = "172.16.0.0/16"

#define private subnet to use
mgt_private_subnets = ["172.16.10.0/24","172.16.11.0/24"]

#define public subnets to use. Note you must specify at least two subnets
mgt_public_subnets = ["172.16.0.0/24","172.16.1.0/24"]

#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "bento"

#specify availability zones to provision your resources. Note the availability zone must match the number of public subnets. Also availability zones depends on the region.
#If you change the region use the corresponding availability zones
mgt_availability_zones = ["us-east-1b","us-east-1c"]


#name of the ssh key imported in the deployment instruction
ssh_key_name = "devops"

#provide the name of the admin user for ssh login
ssh_user = "bento"


#specify the instance type of the bastion host
bastion_instance_type = "t3.medium"

env = "management"

#domain name
domain_name = "bento-tools.org"

ec2_instance_type = "t3.xlarge"

jenkins_name = "jenkins-mgt"

jenkins_private_ip = "172.16.10.100"