public_subnet_ids = [
  "subnet-03bb1c845d35aacc5",
  "subnet-0a575f7e0c97cad77"
]
private_subnet_ids = [
  "subnet-4f35f112",
  "subnet-409a0424"
]
vpc_id = "vpc-29a12251"
stack_name = "gmb"

tags = {
  Project = "GMB"
  CreatedWith = "Terraform"
  POC = "ye.wu@nih.gov"
  Environment = "dev"
}
region = "us-east-1"

#alb
internal_alb = true
certificate_domain_name = "*.cancer.gov"
domain_name = "cancer.gov"

#ecr
create_ecr_repos = true
ecr_repo_names = ["backend","frontend","auth","files"]

#ecs
fargate_security_group_ports = ["80","443","3306","7473","7474","9200","7687"]
application_subdomain = "gmb"
microservices  = {
  frontend = {
    name = "frontend"
    port = 80
    health_check_path = "/"
    priority_rule_number = 22
    image_url = "cbiitssrepo/bento-frontend:latest"
    cpu = 256
    memory = 512
    path = "/*"
    number_container_replicas = 1
  },
  backend = {
    name = "backend"
    port = 8080
    health_check_path = "/ping"
    priority_rule_number = 20
    image_url = "cbiitssrepo/bento-backend:latest"
    cpu = 512
    memory = 1024
    path = "/v1/graphql/*"
    number_container_replicas = 1
  },
  auth = {
    name = "auth"
    port = 8082
    health_check_path = "/api/auth/ping"
    priority_rule_number = 21
    image_url = "cbiitssrepo/bento-auth:latest"
    cpu = 256
    memory = 512
    path = "/api/auth/*"
    number_container_replicas = 1
  },
  files = {
    name = "files"
    port = 8081
    health_check_path = "/api/files/ping"
    priority_rule_number = 19
    image_url = "cbiitssrepo/bento-filedownloader:latest"
    cpu = 256
    memory = 512
    path = "/api/files/*"
    number_container_replicas = 1
  },
  files = {
    name = "users"
    port = 8083
    health_check_path = "/api/users/ping"
    priority_rule_number = 18
    image_url = "cbiitssrepo/bento-auth:latest"
    cpu = 256
    memory = 512
    path = "/api/users/*"
    number_container_replicas = 1
  }
}

#opensearch
create_opensearch_cluster = true
opensearch_ebs_volume_size = 200
opensearch_instance_type = "t3.medium.search"
opensearch_version = "OpenSearch_1.2"
allowed_ip_blocks = ["10.208.8.0/21"]
create_os_service_role = true
opensearch_instance_count = 1
create_cloudwatch_log_policy = false


#neo4j db will not be create
create_db_instance = false
katalon_security_group_id = ""
bastion_host_security_group_id = ""
db_subnet_id = ""
db_instance_volume_size = 80
ssh_user = ""
db_private_ip = ""
db_iam_instance_profile_name = ""
ssh_key_name = ""
public_ssh_key_ssm_parameter_name = ""

#dns
create_dns_record = false