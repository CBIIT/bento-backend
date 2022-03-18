#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "ICDC"
  Environment = "Prod"
  Region = "us-east-1"
}
#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "icdc"


elasticsearch_instance_type = "t3.medium.elasticsearch"

private_subnet_ids = ["subnet-8de37de9"]

vpc_id = "vpc-0bab2873"

subnet_ip_block = ["10.208.30.0/23","10.208.26.0/23"]
create_es_service_role = false
