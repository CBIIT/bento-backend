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
variable "jenkins_home" {
  default = "/local/jenkins"  
}

variable "k9dc_home" {
  default = "/local/k9dc"  
}
variable "docker_home" {
  default = "/local/docker"  
}
variable "app_name" {
  default = "ICDC"
}


variable "k9dc_instance_type" {
  default = "t2.medium"
}

variable "neo4j_instance_type" {
  default = "t2.medium"
}
variable "neo4j_home" {
  default = "/local/neo4j"
}
variable "insecure_bastion_no_strict_host_key_checking" {
  default = false
}
variable "neo4j_version" {
  default = "3.5.0.3"
}
variable "health_check" {
    type = "map"
    default = {
        k9dc    = "/"
        neo4j   = "/"
    }
}


variable "forward_protocol_k9dc" {
  default = {
    k9dc = "HTTP"
  }
}

variable "forward_protocol_neo4j" {
  default = {
    neo4j = "HTTPS"
  }
}
variable "forward_protocol_bolt" {
  default = {
    neo4j = "HTTPS"
  }
}
variable "listener_port_k9dc" {
  default = {
    ssl = 443
  }
}
variable "listener_port_neo4j" {
  default = {
    ssl = 7473
  }
}

variable "listener_port_bolt" {
  default = {
    ssl = 7687
  }
}
variable "alb_rules_k9dc" {
    type = "map"
    default = {
        k9dc    = 80
    }
}

variable "alb_rules_neo4j" {
    type = "map"
    default = {
        neo4j   = 7473 
    }
}
variable "alb_rules_bolt" {
    type = "map"
    default = {
        bolt    = 7687  
    }
}
variable "health_check_k9dc" {
  default = {
    port = 80
    path = "/"
  }
}
variable "health_check_neo4j" {
  default = {
    port = 7474
    path = "/"
  }
}

variable "health_check_bolt" {
  default = {
    port = 7474
    path = "/"
  }
}

variable "k9dc_name" {
  default = "k9dc"
}
variable "neo4j_name" {
  default = "neo4j"
}
variable "domain" {
  default = "essential-dev.com"
}
variable "extra_userdata_merge" {
  default     = "list(append)+dict(recurse_array)+str()"
  description = "Control how cloud-init merges user-data sections"
}
variable "environment" {
  default = "sandbox"
}
