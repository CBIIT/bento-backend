variable "stack_name" {
  description = "The name for terraform stack "
}

variable "profile" {
  default = "icdc"
}

variable "org_name" {
  description = "The name for terraform stack "
  default     = "ctdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}

variable "alb_idle_timeout" {
  default = 60
}

variable "jenkins_private_ip" {
}

variable "appserver_private_ip1" {
}

variable "appserver_private_ip2" {
}

variable "neo4j_private_ip" {
}
variable "ecs_private_ip" {
  
}

variable "jenkins_instance_type" {
  # default = "t2.micro"
  default = "t3.medium"
}

variable "svc_jenkins" {
}

variable "domain" {
}

variable "health_check_jenkins" {
  default = {
    port = 80
    path = "/login"
  }
}

variable "appserver_instance_type" {
  default = "t3.small"
}
variable "ecs_instance_type" {
  default = "t3.medium"
}

variable "svc_app" {
}

variable "svc_frontend" {
}

variable "svc_backend" {
}


variable "health_check_app" {
  default = {
    port = 80
    path = "/"
  }
}

variable "neo4j_instance_type" {
  default = "t3.medium"
}

variable "svc_neo4j" {
}

variable "health_check_neo4j" {
  default = {
    port = 7474
    path = "/"
  }
}

variable "health_check_frontend" {
  default = {
    port = 80
    path = "/"
  }
}

variable "health_check_backend" {
  default = {
    port = 8080
    path = "/ping"
  }
}

variable "rule_priority_neo4j" {
  default = "110"
}

variable "rule_priority_jenkins" {
  default = "120"
}

variable "rule_priority_app" {
  default = "130"
}

variable "rule_priority_frontend" {
  default = "150"
}
variable "rule_priority_backend" {
  default = "140"
}

variable "ecs_role_name" {
  default     = "ecs_instance_role"
  description = "The role name for ecs iam role"
}

variable "ecs_cluster_name" {
  default = "ctdc-ecs"
}

variable "max_ec2_instance_size" {
  default = 2
}

variable "desired_ec2_instance_capacity" {
  default = 1
}

variable "min_ec2_instance_size" {
  default = 1
}

variable index_document {
  default = "index.html"
}
variable "error_document" {
  default = "access_log"
}
variable site {
  default = "bento"
}
variable "tags" {
  description = "tags"
  type = map(string)
}