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
app_name = "vote"
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

allowed_subnet_ip_block = ["172.18.0.0/16","172.16.0.219/32"]
application_subdomain = "vote"
elasticsearch_version = "OpenSearch_1.1"

elasticsearch_instance_type = "t3.medium.elasticsearch"
create_es_service_role = false
app_ecr_registry_names = ["backend","frontend","auth","files"]
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
  }
}