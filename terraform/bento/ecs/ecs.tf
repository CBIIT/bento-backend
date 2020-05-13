data "aws_ssm_parameter" "vault_password" {
  name = "vault_password"
  depends_on = [aws_ssm_parameter.vault]
}

data "template_cloudinit_config" "ecs_userdata" {
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
    content      = "${file("ssm.sh")}"
    # merge_type   = "${var.extra_userdata_merge}"
  }
}

resource "aws_launch_configuration" "ecs-launch-config" {
  name                        = "${var.stack_name}-ecs-launch-configuration"
  image_id                    = data.terraform_remote_state.network.outputs.centos_ami
  instance_type               = var.ecs_instance_type
  iam_instance_profile        = aws_iam_instance_profile.ecs-instance-profile.id
  security_groups             = [aws_security_group.base_security_group.id, aws_security_group.ecs_security_group.id]
  associate_public_ip_address = "false"
  key_name                    = data.terraform_remote_state.network.outputs.ssh_keypair
  user_data                   = data.template_cloudinit_config.ecs_userdata.rendered
  root_block_device {
    volume_type           = "standard"
    volume_size           = 40
    delete_on_termination = true
  }

  lifecycle {
    create_before_destroy = true
  }

}

resource "aws_autoscaling_group" "ecs-asg" {
  # name                 = join("-",[var.stack_name,"ecs","autoscaling","group"])
  name                 = "ecs"
  max_size             = var.max_ec2_instance_size
  min_size             = var.min_ec2_instance_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = [data.terraform_remote_state.network.outputs.app_private_subnent_a_id]
  launch_configuration = aws_launch_configuration.ecs-launch-config.name
  target_group_arns    = [data.aws_lb_target_group.frontend.arn,data.aws_lb_target_group.frontend.arn]
  # health_check_type    = "ELB"
  tag {
    key = "Name"
    value = "ctdc_ecs"
    propagate_at_launch = true
  }
}

module "alb_frontend_config" {
  source       = "../modules/alb_svc_config"
  stack        = var.stack_name
  svc_name     = var.svc_frontend
  vpc_id       = data.terraform_remote_state.network.outputs.vpc_id
  alb_arn      = aws_alb.alb.arn
  domain_name  = var.domain
  health_check = var.health_check_frontend
  priority     = var.rule_priority_frontend
  listener_arn = aws_lb_listener.alb_listener_https.arn
}


module "alb_backend_config" {
  source       = "../modules/alb_svc_config"
  stack        = var.stack_name
  svc_name     = var.svc_backend
  vpc_id       = data.terraform_remote_state.network.outputs.vpc_id
  alb_arn      = aws_alb.alb.arn
  domain_name  = var.domain
  target_port  = "8080"
  health_check = var.health_check_backend
  priority     = var.rule_priority_backend
  listener_arn = aws_lb_listener.alb_listener_https.arn
}

data "aws_lb_target_group" "frontend" {
  name       = lookup(var.svc_frontend,"name")
  depends_on = [module.alb_frontend_config]
}

data "aws_lb_target_group" "backend" {
  name       = lookup(var.svc_backend,"name")
  depends_on = [module.alb_backend_config]
}


#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "ssm_doc_boostrap" {
  name          = "bootstrap-ecs"
  document_type = "Command"
  document_format = "YAML"
  content = <<DOC
---
schemaVersion: '2.2'
description: State Manager Bootstrap Example
parameters: {}
mainSteps:
- action: aws:runShellScript
  name: configureServer
  inputs:
    runCommand:
    - set -ex
    - cd /tmp
    - rm -rf icdc-devops || true
    - yum -y install epel-release
    - yum -y install wget git python-setuptools python-pip
    - pip install ansible==2.7.0 boto boto3 botocore
    - git clone https://github.com/CBIIT/icdc-devops
    - cd icdc-devops && git checkout master
    - cd comets
    - ansible-playbook ecs-agent.yml --skip-tags master -e ecs_cluster_name="${var.ecs_cluster_name}" -e env="${var.stack_name}"
DOC
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ssm-document")
  },
  var.tags,
  )
}
#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "bento_doc" {
  name          = "bootstrap-bento"
  document_type = "Command"
  document_format = "YAML"
  content = <<DOC
---
schemaVersion: '2.2'
description: State Manager Bootstrap Example
parameters: {}
mainSteps:
- action: aws:runShellScript
  name: configureServer
  inputs:
    runCommand:
    - set -ex
    - sleep 120
    - systemctl restart docker
  DOC
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"bento-bootstrap")
  },
  var.tags,
  )
}

resource "aws_ssm_document" "bootstrap" {
  document_format = "YAML"
  document_type = "Command"
  name = "boostrap-ecs-instances"
  content = <<DOC
---
schemaVersion: '2.2'
description: Bootstrap ecs instances
parameters: {}
mainSteps:
- action: aws:runDocument
  name: configureECSAgent
  inputs:
    documentType: SSMDocument
    documentPath: bootstrap-ecs
    documentParameters: "{}"
- action: aws:runDocument
  name: configureicrp
  inputs:
    documentType: SSMDocument
    documentPath: bootstrap-bento
    documentParameters: "{}"

DOC
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"bootstrap-ecs-instances")
  },
  var.tags,
  )
}

resource "aws_ssm_association" "ecs" {
  name = aws_ssm_document.bootstrap.name
  targets {
    key   = "tag:aws:autoscaling:groupName"
    values = ["ecs"]
  }
  output_location {
    s3_bucket_name = "icdc-sandbox-runcmd"
  }
}
