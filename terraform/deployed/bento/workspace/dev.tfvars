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
region = "us-west-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "bento"

#specify availability zones to provision your resources. Note the availability zone must match the number of public subnets. Also availability zones depends on the region.
#If you change the region use the corresponding availability zones
availability_zones = ["us-west-1b","us-west-1c"]

#provide the name of the ecs cluster 
ecs_cluster_name = "bento"

#specify the number of container replicas, minimum is 1
container_replicas = 2

#This is a port number for the bento-frontend 
frontend_container_port = 80

#This a port number for bento-backend
backend_container_port = 8080

#specify the maximum and minimun number of instances in auto-scalling group
max_size = 2
min_size = 1

#provide name for the auto-scalling-groups
frontend_asg_name = "frontend"

desired_ec2_instance_capacity = 2

#cutomize the volume size for all the instances created except database
instance_volume_size = 40

#name of the ssh key imported in the deployment instruction
ssh_key_name = "devops"

#specify the aws compute instance type for the bento
fronted_instance_type = "t3.medium"

#provide the name of the admin user for ssh login
ssh_user = "bento"

#specify the aws compute instance type for the database
database_instance_type =  "c5.xlarge"

#name of the database
database_name = "neo4j"

#specify the volume size for the database
db_instance_volume_size = 50

#alb priority rule number. This can be left as default
alb_rule_priority = 100

#specify neo4j database
database_password = "custodian"

#specify the instance type of the bastion host
bastion_instance_type = "t3.medium"

env = "dev"

#specify domain name
domain_name = "bento-tools.org"
#name of the application
app_name = "bento"

#the port on which the frontend app listens
app_port = 80

#specify private ip of the db instance
db_private_ip = "172.17.11.25"

remote_state_bucket_name = "bento-demo-terraform-remote-state"