variable "profile" {
  description = "Profile for launching vm"
  default = "icdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}
variable "insecure_no_strict_host_key_checking" {
  default = false
}
variable "insecure_bastion_no_strict_host_key_checking" {
  default = false
}
variable "agent_instance_type" {
  default = "t2.medium"
}
variable "jenkins_instance_type" {
  default = "t2.medium"
}
variable "docker_home" {
  default = "/local/docker"  
}

variable "jenkins_home" {
  default = "/local/jenkins"  
}
variable "secrets_home" {
  default = "/local/secret"  
}
variable "alb_rules_map_jenkins" {
    type = "map"
    default = {
        jenkins   = 80
    }
}
variable "domain" {
  default = "essential-dev.com"
}
variable "jenkins_name" {
  default = "jenkins"
}
variable "forward_protocol_jenkins" {
  default = {
    jenkins = "HTTP"
  }
}
variable "health_check_jenkins" {
  default = {
    port = 80
    path = "/login"
  }
}