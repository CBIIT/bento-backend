public_subnets = []
private_subnets = [
  "subnet-4f35f112",
  "subnet-409a0424"
]
vpc_id = "vpc-29a12251"
stack_name = "icdc"
app_name = "icdc"
domain_name = "cancer.gov"
tags = {
  Project = "ICDC"
  CreatedWith = "Terraform"
  POC = "ye.wu@nih.gov"
}
certificate_domain_name = "*.cancer.gov"
backend_container_port = 8080
frontend_container_port = 80
frontend_container_image_name = "cbiitssrepo/bento-frontend"
backend_container_image_name = "cbiitssrepo/bento-backend"
internal_alb = true
app_sub_domain = "caninecommons"
