

resource "aws_security_group" "jenkins_security_group" {
  name = "${var.stack_name}-jenkins-sg"
  description = "jenkins security group"
  vpc_id      = "${data.terraform_remote_state.network.vpc_id}"

  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]    
  }
  
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]  
  }

  tags = {
    Name = "${var.stack_name}-jenkins-sg"
    ByTerraform = "true"
  }
}




#
resource "aws_security_group" "docker_security_group" {
  name        = "${var.stack_name}-docker-sg"
  description = "docker security group for jenkins"
  vpc_id      = "${data.terraform_remote_state.network.vpc_id}"
  
   ingress {
    from_port   =   2375
    to_port     =   2375
    protocol    =   "tcp"
    security_groups =   ["${aws_security_group.jenkins_security_group.id}"]
  }

   ingress {
    from_port   =   2376
    to_port     =   2376
    protocol    =   "tcp"
    security_groups =   ["${aws_security_group.jenkins_security_group.id}"]
  }

  tags = {
    Name = "${var.stack_name}-docker-sg"
    ByTerraform = "true"
  }
}

resource "aws_instance" "jenkins" {
    ami                         = "${data.terraform_remote_state.network.centos_ami}"
    instance_type               = "${var.jenkins_instance_type}"
    subnet_id                   = "${data.terraform_remote_state.network.app_private_subnent_a_id}"
    key_name                    = "${data.terraform_remote_state.network.ssh_keypair}"
    vpc_security_group_ids      = ["${aws_security_group.base_security_group.id}", "${aws_security_group.jenkins_security_group.id}", "${aws_security_group.docker_security_group.id}"]
    private_ip                  = "${var.jenkins_private_ip}"

    tags = {
        Name                    = "${var.stack_name}-jenkins"
        Org                     = "${var.org_name}"
        ByTerraform             = "true"
    }

}


# resource "aws_eip_association" "icdc_jenkins_eip" {
#   instance_id   = "${aws_instance.jenkins.id}"
#   allocation_id = "${data.terraform_remote_state.network.icdc_jenkins_eip}"
# }
module "alb_jenkins_config" {
  source                = "../modules/alb_svc_config"
  stack                 = "${var.stack_name}"
  svc_name              = "${var.svc_jenkins}"
  vpc_id                = "${data.terraform_remote_state.network.vpc_id}"
  alb_arn               = "${aws_alb.alb.arn}"
  domain_name           = "${var.domain}"
  health_check          = "${var.health_check_jenkins}"
  priority              = "${var.rule_priority_jenkins}"
}

data "aws_lb_target_group" "jenkins" {
  name = "${var.stack_name}-${var.svc_jenkins}"
  depends_on            = ["module.alb_jenkins_config"]
}
resource "aws_lb_target_group_attachment" "alb_attach_jenkins" {
  target_group_arn = "${data.aws_lb_target_group.jenkins.arn}"
  # target_group_arn = "${module.alb_listener_rules_jenkins.alb_target_group_arn}"
  target_id        = "${aws_instance.jenkins.id}"
  port             = "80"
}