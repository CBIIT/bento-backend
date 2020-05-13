resource "aws_security_group" "base_security_group" {
  name        = "${var.stack_name}-base-sg"
  description = "base security group"
  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id

  # allow SSH from bastion host
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"

    security_groups = [data.terraform_remote_state.network.outputs.bastion_security_id]
  }

  #allow icmp 

  ingress {
    from_port = -1
    to_port   = -1
    protocol  = "icmp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  #allow all outgoing protocols
  egress {
    from_port = "0"
    to_port   = "0"
    protocol  = "-1"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags = {
    Name        = "${var.stack_name}-base-sg"
    ByTerraform = "true"
  }
}

resource "aws_security_group" "ecs_security_group" {
  name        = "${var.stack_name}-ecs-sg"
  description = "ecs security group"
  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  
  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  egress {
    from_port = "0"
    to_port   = "0"
    protocol  = "-1"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags = {
    Name        = "${var.stack_name}-ecs-sg"
    ByTerraform = "true"
  }
}