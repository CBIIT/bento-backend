#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "Bento"
  Environment = "prod"
  Region = "us-east-1"
}
#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"

#specify the name you will like to call this project.
stack_name = "bento"

#specify domain name
domain_name = "bento-dev"
#name of the application
remote_state_bucket_name = "bento-terraform-remote-state"

elasticsearch_instance_type = "t3.medium.elasticsearch"

create_es_service_role = true
