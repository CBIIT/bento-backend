#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
}
#specify vpc cidr 
vpc_cidr_block = "10.0.0.0/16"

#define private subnet to use
private_subnets = ["10.0.10.0/24"]

#define public subnets to use. Note you must specify at least two subnets
public_subnets = ["10.0.0.0/24","10.0.1.0/24"]

#enter the region in which your aws resources will be provisioned
region = "us-west-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "demo"

#specify availability zones to provision your resources. Note the availability zone must match the number of public subnets. Also availability zones depends on the region.
#If you change the region use the corresponding availability zones
availaiblity_zones = ["us-west-1b","us-west-1c"]

#provide the name of the ecs cluster 
ecs_cluster_name = "bento"

#specify the number of container replicas, minimum is 1
container_replicas = 1

#This is a port number for the bento-frontend 
frontend_container_port = 80

#This a port number for bento-backend
backend_container_port = 8080

#specify the maximum and minimun number of instances in auto-scalling group
max_size = 1
min_size = 1

#provide name for the auto-scalling-groups
frontend_asg_name = "frontend"

desired_ec2_instance_capacity = 1

#cutomize the volume size for all the instances created except database
instance_volume_size = 40

#name of the ssh key imported in the deployment instruction
ssh_key_name = "demo-ssh-key"

#specify the aws compute instance type for the bento
fronted_instance_type = "t3.medium"

#provide the name of the admin user for ssh login
ssh_user = "bento"

#availability zone 
availability_zone = "us-east-2a"

#specify the aws compute instance type for the database
database_instance_type =  "t3.medium"

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

#specify the url of the bento backend repository
backend_repo = "https://github.com/CBIIT/bento-demo-backend"

#specify the url of the bento frontend repository
frontend_repo = "https://github.com/CBIIT/bento-demo-frontend"

#specify the url of the bento data repository

data_repo = "https://github.com/CBIIT/bento-demo-data-model"

#specify dataset to be used

dataset = "Bento_Mock_Data_for_PACT1"

#specify data schema model file name if changed from default
model_file_name = "bento_tailorx_model_file.yaml"

# specify data schema properties file if changed from default
property_filename = "bento_tailorx_model_properties.yaml"
