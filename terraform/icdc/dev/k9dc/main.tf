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
data "aws_ssm_parameter" "admin_password" {
  name = "admin_password"
}
data "aws_ssm_parameter" "neo4j_password" {
  name = "neo4j_password"
}
data "aws_ssm_parameter" "sumologic_accessid" {
  name = "sumologic_accessid"
}
data "aws_ssm_parameter" "sumologic_accesskey" {
  name = "sumologic_accesskey"
}
#retrieve secrets
locals {
  devops_user = "${secret_resource.devops_user.value}"
  devops_ssh_key_pub = "${secret_resource.devops_ssh_key_public.value}"
  devops_ssh_key     = "${secret_resource.devops_ssh_key_private.value}"
  # icdcTomcat_password = "${secret_resource.icdcTomcat_password.value}"
  new_relic_license_key = "${secret_resource.new_relic_license_key.value}"
  neo4j_password = "${secret_resource.neo4j_password.value}"
}

data "template_cloudinit_config" "k9dc_userdata" {
  gzip          = false
  base64_encode = false
  part {
    content = <<EOF
#cloud-config
---
users:
  - name: "${local.devops_user}"
    gecos: "${local.devops_user}"
    sudo: ALL=(ALL) NOPASSWD:ALL
    groups: wheel
    shell: /bin/bash
    ssh_authorized_keys:
    - "${local.devops_ssh_key_pub}"
EOF
  }

  part {
    # filename     = "k9dc.sh"
    content_type = "text/x-shellscript"
    content      = "${file("k9dc.sh")}"
    # merge_type   = "${var.extra_userdata_merge}"
  }
}

data "template_cloudinit_config" "neo4j_userdata" {
  gzip          = false
  base64_encode = false
  part {
    content = <<EOF
#cloud-config
---
users:
  - name: "${local.devops_user}"
    gecos: "${local.devops_user}"
    sudo: ALL=(ALL) NOPASSWD:ALL
    groups: wheel
    shell: /bin/bash
    ssh_authorized_keys:
    - "${local.devops_ssh_key_pub}"
EOF
  }

  part {
    # filename     = "neo4j.sh"
    content_type = "text/x-shellscript"
    content      = "${file("neo4j.sh")}"
    # merge_type   = "${var.extra_userdata_merge}"
  }
}
#Lets provision k9dc

resource "aws_launch_configuration" "k9dc_autoscale_launch" {
  image_id                    = "${data.terraform_remote_state.network.centos_ami}"
  instance_type               = "${var.k9dc_instance_type}"
  iam_instance_profile        = "${data.terraform_remote_state.network.s3_role_profile_name}"
  security_groups             = ["${data.terraform_remote_state.network.app_security_id}"]
  key_name                    = "${data.terraform_remote_state.network.ssh_keypair}"
  user_data                   = "${data.template_cloudinit_config.k9dc_userdata.rendered}"
  lifecycle {
    create_before_destroy         = true
  }
}

resource "aws_launch_configuration" "neo4j_autoscale_launch" {
  image_id                    = "${data.terraform_remote_state.network.centos_ami}"
  instance_type               = "${var.neo4j_instance_type}"
  iam_instance_profile        = "${data.terraform_remote_state.network.s3_role_profile_name}"
  security_groups             = ["${data.terraform_remote_state.network.db_security_id}"]
  key_name                    = "${data.terraform_remote_state.network.ssh_keypair}"
  user_data                   = "${data.template_cloudinit_config.neo4j_userdata.rendered}"
  lifecycle {
    create_before_destroy         = false
  }
  # root_block_sandboxice {
  #   volume_size           = 60
  #   delete_on_termination = true
  # }
}
resource "aws_autoscaling_group" "k9dc_autoscale_group" {
  name                 = "k9dc"
  launch_configuration = "${aws_launch_configuration.k9dc_autoscale_launch.id}"
  vpc_zone_identifier = ["${data.terraform_remote_state.network.app_private_subnent_a_id}"]
  min_size = 2
  max_size = 2
  tag {
    key = "Name"
    value = "icdc_k9dc"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_group" "neo4j_autoscale_group" {
  name                 = "neo4j"
  launch_configuration = "${aws_launch_configuration.neo4j_autoscale_launch.id}"
  vpc_zone_identifier = ["${data.terraform_remote_state.network.db_private_subnent_a_id}"]
  min_size = 1
  max_size = 1
  tag {
    key = "Name"
    value = "icdc_neo4j"
    propagate_at_launch = true
  }
}

module "sandbox_icdc_alb_listener_rules_k9dc" {
  source                = "../../modules/alb_listener_rules"
  services_map          = "${var.alb_rules_k9dc}"
  tag_name              = "app"
  vpc_id                = "${data.terraform_remote_state.network.vpc_id}"
  alb_arn               = "${data.terraform_remote_state.network.alb_arn}"
  forward_protocol        = "${var.forward_protocol_k9dc}"
  listener_port         = "${var.listener_port_k9dc}"
  domain                = "${var.domain}"
  health_check          = "${var.health_check_k9dc}"
  create_listener       = true
}

module "sandbox_icdc_alb_listener_rules_neo4j" {
  source                = "../../modules/alb_listener_rules"
  services_map          = "${var.alb_rules_neo4j}"
  tag_name              = "db"
  vpc_id                = "${data.terraform_remote_state.network.vpc_id}"
  alb_arn               = "${data.terraform_remote_state.network.alb_arn}"
  forward_protocol      = "${var.forward_protocol_neo4j}"
  listener_port         = "${var.listener_port_neo4j}"
  domain                = "${var.domain}"
  health_check          = "${var.health_check_bolt}"
  create_listener       = true
}
module "sandbox_icdc_alb_listener_rules_neo4j_bolt" {
  source                = "../../modules/alb_listener_rules"
  services_map          = "${var.alb_rules_bolt}"
  tag_name              = "bolt"
  vpc_id                = "${data.terraform_remote_state.network.vpc_id}"
  alb_arn               = "${data.terraform_remote_state.network.alb_arn}"
  forward_protocol      = "${var.forward_protocol_bolt}"
  listener_port         = "${var.listener_port_bolt}"
  domain                = "${var.domain}"
  health_check          = "${var.health_check_bolt}"
  create_listener       = true
}
data "aws_lb_target_group" "k9dc" {
  name = "${element(keys(var.alb_rules_k9dc), 0)}"
  depends_on  =  ["module.sandbox_icdc_alb_listener_rules_k9dc"]
}

data "aws_lb_target_group" "neo4j" {
  name = "neo4j"
  depends_on  =  ["module.sandbox_icdc_alb_listener_rules_neo4j"]
}
data "aws_lb_target_group" "bolt" {
  name = "bolt"
  depends_on  =  ["module.sandbox_icdc_alb_listener_rules_neo4j"]
}

resource "aws_autoscaling_attachment" "k9dc_alb_autoscale" {
  alb_target_group_arn      =   "${data.aws_lb_target_group.k9dc.arn}"
  autoscaling_group_name    =   "${aws_autoscaling_group.k9dc_autoscale_group.id}"
}


resource "aws_autoscaling_attachment" "neo4j_alb_autoscale" {
  alb_target_group_arn      =   "${data.aws_lb_target_group.neo4j.arn}"
  autoscaling_group_name    =   "${aws_autoscaling_group.neo4j_autoscale_group.id}"
}
resource "aws_lb_target_group_attachment" "alb_attach_bolt" {
  target_group_arn = "${data.aws_lb_target_group.bolt.arn}"
  target_id        = "${data.aws_instance.neo4j.id}"
  port             = "7687"
}

data "aws_instance" "neo4j" {
  instance_tags {
     Name = "icdc_neo4j"
  }
  depends_on = ["aws_autoscaling_group.neo4j_autoscale_group"]
}
data "aws_instances" "k9dc" {
  instance_tags {
     Name = "icdc_k9dc"
  }
  depends_on = ["aws_autoscaling_group.k9dc_autoscale_group"]
}

# module "sandbox_route_53_alias" {
#   source          = "../../modules/route-53"
#   # domain_name     = "${var.domain_name}"
#   dns_name        = "${data.terraform_remote_state.network.alb_dns}"
#   alb_zone_id     = "${data.terraform_remote_state.network.zone_id}"
#   hostnames       = ["${var.k9dc_name}","${var.neo4j_name}"]
# }

resource "aws_ssm_document" "k9dc" {
  name          = "bootstrap_k9dc"
  document_type = "Command"

  content = <<DOC
  {  
   "schemaVersion":"1.2",
   "description":"Bootstrap k9dc",
   "parameters":{  
   },
   "runtimeConfig":{  
      "aws:runShellScript":{  
         "properties":[  
            {  
               "id":"0.aws:runShellScript",
               "runCommand":[  
                  "yum -y install wget git python-setuptools s3cmd at",
                  "easy_install pip",
                  "pip install ansible boto boto3 botocore",
                  "git clone https://github.com/vdonkor/ctn.git",
                  "echo \"${data.aws_ssm_parameter.admin_password.value}\" > ./ctn/mypassword",
                  "ansible-playbook -i ./ctn/hosts --vault-password-file ./ctn/mypassword ./ctn/k9dc.yml -e env=\"${var.environment}\" -e access_id=\"${data.aws_ssm_parameter.sumologic_accessid.value}\" -e access_key=\"${data.aws_ssm_parameter.sumologic_accesskey.value}\""
               ]
            }
         ]
      }
   }
}
DOC
}

resource "aws_ssm_association" "k9dc" {
  name = "${aws_ssm_document.k9dc.name}"
  targets {
    key   = "tag:aws:autoscaling:groupName"
    values = ["k9dc"]
  }
  output_location {
    s3_bucket_name = "icdc-sandbox-runcmd"
  }
}

resource "aws_ssm_document" "neo4j" {
  name          = "bootstrap_neo4j"
  document_type = "Command"

  content = <<DOC
  {  
   "schemaVersion":"1.2",
   "description":"Bootstrap neo4j",
   "parameters":{  
   },
   "runtimeConfig":{  
      "aws:runShellScript":{  
         "properties":[  
            {  
               "id":"0.aws:runShellScript",
               "runCommand":[
                  "yum -y install wget git python-setuptools",
                  "easy_install pip",
                  "pip install ansible",
                  "git clone https://github.com/vdonkor/ctn.git",
                  "echo \"${data.aws_ssm_parameter.admin_password.value}\" > ./ctn/mypassword",
                  "ansible-playbook -i ./ctn/hosts --vault-password-file ./ctn/mypassword ./ctn/neo4j.yml -e neo4j_password=\"${data.aws_ssm_parameter.neo4j_password.value}\" -e env=\"${var.environment}\" -e access_id=\"${data.aws_ssm_parameter.sumologic_accessid.value}\" -e access_key=\"${data.aws_ssm_parameter.sumologic_accesskey.value}\""
                
               ]
            }
         ]
      }
   }
}
DOC
}
resource "aws_ssm_association" "neo4j" {
  name = "${aws_ssm_document.neo4j.name}"
  targets {
    key   = "tag:aws:autoscaling:groupName"
    values = ["neo4j"]
  }
  output_location {
    s3_bucket_name = "icdc-sandbox-runcmd"
  }
  
}