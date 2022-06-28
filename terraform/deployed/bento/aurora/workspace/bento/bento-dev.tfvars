db_subnet_ids = [
  "subnet-09b0c7407416d4730",
  "subnet-07d177a4d9df5cd32"
]
vpc_id = "vpc-08f154f94dc8a0e34"
stack_name = "bento"

tags = {
  Project = "bento"
  CreatedWith = "Terraform"
  POC = "ye.wu@nih.gov"
  Environment = "dev"
}
region = "us-east-1"
allowed_ip_blocks = ["172.18.0.0/16","172.16.0.219/32"]
db_engine_mode = "provisioned"
db_engine_version = "8.0"
db_engine_type = "aurora-mysql"
master_username = "bento"
db_instance_class = "db.serverless"