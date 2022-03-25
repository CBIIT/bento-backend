#define any tags appropriate to your environment
tags = {
  ManagedBy = "terraform"
  Project = "CTDC"
  Environment = "DEV"
  POC = "ye.wu@nih.gov"
}
#enter the region in which your aws resources will be provisioned
region = "us-east-1"

#specify the name you will like to call this project.
stack_name = "ctdc"

private_subnet_ids = ["< UPDATE >"]

vpc_id = "< UPDATE >"

subnet_ip_block = ["< UPDATE >"]

create_es_service_role = false
