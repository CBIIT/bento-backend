#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "Bento"
  Environment = "poc"
  Region = "us-east-1"
  ShutdownInstance = "Yes"
}
#enter the region in which your aws resources will be provisioned
region = "us-east-1"
#specify your aws credential profile. Note this is not IAM role but rather profile configured during AWS CLI installation
profile = "icdc"
#specify the name you will like to call this project.
stack_name = "bento"
desired_ec2_instance_capacity = 1
#specify the maximum and minimun number of instances in auto-scalling group
max_size =2
min_size = 1

