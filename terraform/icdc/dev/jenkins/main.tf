#set aws provider
provider "aws" {
  region  = "${var.region}"
  profile = "${var.profile}"
}

data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket  = "icdc-sandbox-terraform-state"
    key     = "state/sandbox/terraform.tfstate"
    encrypt = "true"
    region  = "us-east-1"
  }
}

data "terraform_remote_state" "k9dc" {
  backend = "s3"
  config = {
    bucket  = "icdc-sandbox-terraform-state"
    key     = "state/sandbox/terraform_k9dc.tfstate"
    encrypt = "true"
    region  = "us-east-1"
  }
}


locals {
  devops_user = "${secret_resource.devops_user.value}"
  devops_ssh_key_pub = "${secret_resource.devops_ssh_key_public.value}"
  devops_ssh_key     = "${secret_resource.devops_ssh_key_private.value}"
  sshkey     = "${secret_resource.devops_ssh_key_private.value}"
  icdcTomcat_password = "${secret_resource.icdcTomcat_password.value}"
  jenkinsAdmin = "${secret_resource.admin_password.value}"
  vdonkor = "${secret_resource.github_password.value}"
  bearer = "${secret_resource.bearer.value}"
  neo4j = "${secret_resource.neo4j_password.value}"
  slack_url = "${secret_resource.slack_url.value}"
}


resource "aws_instance" "agent" {
    ami                         = "${data.terraform_remote_state.network.centos_ami}"
    instance_type               = "${var.agent_instance_type}"
    subnet_id                   = "${data.terraform_remote_state.network.app_private_subnent_a_id}"
    key_name                    = "${data.terraform_remote_state.network.ssh_keypair}"
    vpc_security_group_ids      = ["${data.terraform_remote_state.network.docker_security_id}"]
    
    user_data                   = <<-EOF
                              #cloud-config
                              users:
                                - name: "${local.devops_user}"
                                  gecos: "${local.devops_user}"
                                  sudo: ALL=(ALL) NOPASSWD:ALL
                                  groups: wheel
                                  shell: /bin/bash
                                  ssh_authorized_keys:
                                  - "${local.devops_ssh_key_pub}"
                                  EOF
    tags = {
        Name                    = "docker_agent"
        Org                     = "icdc"
        ByTerraform             = "true"
  }
    connection {
    user = "${local.devops_user}"
    private_key  = "${local.devops_ssh_key}"
    host         = "${self.private_ip}"
    bastion_host = "${data.terraform_remote_state.network.bastion_public_ip}"
  }
  provisioner "ansible" {
    plays {
      playbook = {
        file_path = "../../playbook/docker.yml"
        roles_path = ["../../roles"]
      }
      verbose = false
    }
    ansible_ssh_settings {
      insecure_no_strict_host_key_checking = "${var.insecure_no_strict_host_key_checking}"
      connect_timeout_seconds = 60
    }
  }
}

# resource "aws_eip_association" "icdc_docker_eip" {
#   instance_id   = "${aws_instance.agent.id}"
#   allocation_id = "${data.terraform_remote_state.network.icdc_docker_eip}"
# }

resource "aws_instance" "jenkins" {
    ami                         = "${data.terraform_remote_state.network.centos_ami}"
    instance_type               = "${var.jenkins_instance_type}"
    subnet_id                   = "${data.terraform_remote_state.network.app_private_subnent_a_id}"
    key_name                    = "${data.terraform_remote_state.network.ssh_keypair}"
    vpc_security_group_ids      = ["${data.terraform_remote_state.network.jenkins_security_id}"]
    user_data                   = <<-EOF
                              #cloud-config
                              users:
                                - name: "${local.devops_user}"
                                  gecos: "${local.devops_user}"
                                  sudo: ALL=(ALL) NOPASSWD:ALL
                                  groups: wheel
                                  shell: /bin/bash
                                  ssh_authorized_keys:
                                  - "${local.devops_ssh_key_pub}"
                                  EOF
    tags = {
        Name            = "jenkins"
        Org             = "icdc"
        ByTerraform     = "true"
  }
    connection {
    user = "${local.devops_user}"
    private_key = "${local.devops_ssh_key}"
    host         = "${self.private_ip}"
    bastion_host = "${data.terraform_remote_state.network.bastion_public_ip}"
  }
  provisioner "ansible" {
    plays {
      playbook = {
        file_path = "../../playbook/jenkins.yml"
        roles_path = ["../../roles"]
        skip_tags = ["master"]
      }
      
      extra_vars {
        jenkinsAdmin = "${local.jenkinsAdmin}"
      }
      extra_vars {
        jenkins_home = "${var.jenkins_home}"
      }
      extra_vars {
        docker_home = "${var.docker_home}"
      }
      extra_vars {
        vdonkor = "${local.vdonkor}"
      }
      extra_vars {
        bearer = "${local.bearer}"
      }
      extra_vars {
        neo4j = "${local.neo4j}"
      }
      extra_vars {
        slack_url = "${local.slack_url}"
      }
      extra_vars {
        secrets_home = "${var.secrets_home}"
      }
      extra_vars {
        dockerfile = "${var.docker_home}/dockerfile_jenkins"
      }
      extra_vars {
        docker_compose_file = "${var.docker_home}/docker-compose.yml"
      }
      extra_vars {
        neo4j_ip = "${data.terraform_remote_state.k9dc.neo4j_ip}"
      }
      extra_vars {
        tomcat01_ip = "${data.terraform_remote_state.k9dc.tomcat01_ip}"
      }
      extra_vars {
        tomcat02_ip = "${data.terraform_remote_state.k9dc.tomcat02_ip}"
      }
      extra_vars {
        docker_agent_ip = "${aws_instance.agent.private_ip}"
      }
      extra_vars {
        sshkey = "${local.sshkey}"
      }
      verbose = false
    }
    ansible_ssh_settings {
      insecure_no_strict_host_key_checking = "${var.insecure_no_strict_host_key_checking}"
      insecure_bastion_no_strict_host_key_checking = "${var.insecure_bastion_no_strict_host_key_checking}"
      connect_timeout_seconds = 60
    }
  }
}
# resource "aws_eip_association" "icdc_jenkins_eip" {
#   instance_id   = "${aws_instance.jenkins.id}"
#   allocation_id = "${data.terraform_remote_state.network.icdc_jenkins_eip}"
# }
module "sandbox_icdc_alb_listener_rules_jenkins" {
  source                = "../../modules/alb_listener_rules"
  services_map          = "${var.alb_rules_map_jenkins}"
  tag_name              = "jenkins"
  vpc_id                = "${data.terraform_remote_state.network.vpc_id}"
  alb_arn               = "${data.terraform_remote_state.network.alb_arn}"
  domain                = "${var.domain}"
  health_check          = "${var.health_check_jenkins}"
  forward_protocol      = "${var.forward_protocol_jenkins}"
  create_listener       = false
}

data "aws_lb_target_group" "jenkins" {
  name = "jenkins"
  depends_on            = ["module.sandbox_icdc_alb_listener_rules_jenkins"]
}
resource "aws_lb_target_group_attachment" "alb_attach_jenkins" {
  target_group_arn = "${data.aws_lb_target_group.jenkins.arn}"
  target_id        = "${aws_instance.jenkins.id}"
  port             = "${element(values(var.alb_rules_map_jenkins), 0)}"
}
# module "sandbox_route_53_alias" {
#   source          = "../../modules/route-53"
#   # domain_name     = "${var.domain_name}"
#   dns_name        = "${data.terraform_remote_state.network.alb_dns}"
#   alb_zone_id     = "${data.terraform_remote_state.network.zone_id}"
#   hostnames       = ["${var.jenkins_name}"]
# }