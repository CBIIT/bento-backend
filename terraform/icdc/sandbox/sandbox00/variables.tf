variable "profile" {
  # default = "default"
}

variable "stack_name" {
  description = "The name for terraform stack "
  default = "sandbox00"
}

variable "org_name" {
  description = "The name for organization "
  default = "icdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}

variable "alb_idle_timeout" {
  default = 60
}

variable "jenkins_private_ip" {
  default = "172.18.1.100"
}

variable "appserver_private_ip1" {
  default = "172.18.1.101"
}

variable "appserver_private_ip2" {
  default = "172.18.1.102"
}

variable "neo4j_private_ip" {
  default = "172.18.4.103"
}

variable "docker_private_ip" {
  default = "172.18.1.104"
}

variable "jenkins_instance_type" {
  default = "t2.medium"
}

variable "docker_instance_type" {
  default = "t2.medium"
}

variable "svc_jenkins" {
  default = "jenkins"
}

variable "domain" {
  default = "essential-dev.com"
}

variable "health_check_jenkins" {
  default = {
    port = 80
    path = "/login"
  }
}



variable "appserver_instance_type" {
  default = "t2.medium"
}

variable "svc_app" {
  default = "k9dc"
}

variable "health_check_app" {
  default = {
    port = 8080
    path = "/"
  }
}


variable "neo4j_instance_type" {
  default = "t2.medium"
}


variable "svc_neo4j" {
  default = "neo4j"
}

variable "svc_bolt" {
  default = "bolt"
}

variable "health_check_neo4j" {
  default = {
    port = 7474
    path = "/"
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
variable "rule_priority_bolt" {
  default = "140"
}