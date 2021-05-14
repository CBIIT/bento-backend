#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "IN"
  Environment = "dev"
  Region = "us-east-1"
  ShutdownInstance = "Yes"
  POC = "Wei Yu"
}


#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "IN"

#provide the name of the ecs cluster 
ecs_cluster_name = "IN"

#specify the number of container replicas, minimum is 1
container_replicas = 1

#This is a port number for the bento-frontend 
frontend_container_port = 80

#This a port number for bento-backend
backend_container_port = 8080

#This a port number for bento-backend
downloader_container_port = 8081

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
database_instance_type =  "t3.medium"

#name of the database
database_name = "neo4j-4"

#specify the volume size for the database
db_instance_volume_size = 80

#alb priority rule number. This can be left as default
alb_rule_priority = 121
frontend_rule_priority = 121
backend_rule_priority = 120
downloader_rule_priority = 119
env = "dev"

#specify domain name
domain_name = "bento-tools.org"
#name of the application
app_name = "in"

#the port on which the frontend app listens
app_port = 80

#specify private ip of the db instance
db_private_ip = "172.18.11.29"

remote_state_bucket_name = "bento-terraform-remote-state"
redis_node_group = 1