public_subnet_ids = [
  "subnet-03bb1c845d35aacc5",
  "subnet-0a575f7e0c97cad77"
]
private_subnet_ids = [
  "subnet-09b0c7407416d4730",
  "subnet-07d177a4d9df5cd32"
]
vpc_id = "vpc-08f154f94dc8a0e34"
stack_name = "vote"
domain_name = "bento-tools.org"

tags = {
  Project = "bento"
  CreatedWith = "Terraform"
  POC = "ye.wu@nih.gov"
  Environment = "dev"
}
internal_alb = "false"
certificate_domain_name = "*.bento-tools.org"
region = "us-east-1"

fargate_security_group_ports = ["80","443","3306","7473","7474","9200","7687"]
application_subdomain = "vote"
create_ecr_repos = true
elasticsearch_instance_type = "t3.medium.elasticsearch"
ecr_repo_names = ["backend","frontend","auth","files"]
create_dns_record = true
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
  }
}
#opensearch
create_opensearch_cluster = true
opensearch_ebs_volume_size = 200
opensearch_instance_type = "t3.medium"
opensearch_version = "OpenSearch_1.2"
opensearch_allowed_ip_block = ["172.18.0.0/16","172.16.0.219/32"]
create_os_service_role = false