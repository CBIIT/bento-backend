# Public security group
resource "aws_security_group" "public_security_group" {
  name        = "${var.public_sg_name}"
  description = "icdc public facing security group"
  vpc_id      = "${var.vpc_id}"
  
  # allow http traffic
  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  # allow https traffic
  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
#allow everything from the public subnet
  ingress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"

    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
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
 
  ingress {
    from_port = 7474
    to_port   = 7474
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  ingress {
    from_port = 7473
    to_port   = 7473
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  ingress {
    from_port = 7687
    to_port   = 7687
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags = {
    Name = "${var.public_sg_name}"
    ByTerraform = "true"
  }
}

# Private security group
resource "aws_security_group" "db_security_group" {
  name        = "${var.db_sg_name}"
  description = "icdc database security group"
  vpc_id      = "${var.vpc_id}"
  
  # allow SSH from bastion host
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  
  ingress {
    from_port = 7474
    to_port   = 7474
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}","${var.app_subnet_a}","${var.app_subnet_c}"]
  }
  

  ingress {
    from_port = 7473
    to_port   = 7473
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}","${var.app_subnet_a}","${var.app_subnet_c}"]
  }


  ingress {
    from_port = 7687
    to_port   = 7687
    protocol  = "tcp"
   cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}","${var.app_subnet_a}","${var.app_subnet_c}"]
  }

  #allow icmp from public subnet
  ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  # allow all outgoing traffic from private subnet
  egress {
    from_port = "0"
    to_port   = "0"
    protocol  = "-1"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  tags = {
    Name = "${var.db_sg_name}"
    ByTerraform = "true"
  }
}

resource "aws_security_group" "app_security_group" {
  name = "${var.app_sg_name}"
  description = "icdc app security group"
  vpc_id      = "${var.vpc_id}"
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }



  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }

  
  
    ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }

  #allow all outgoing traffic 
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.app_sg_name}"
    ByTerraform = "true"
  }
}

resource "aws_security_group" "jenkins_security_group" {
  name = "${var.jenkins_sg_name}"
  description = "icdc jenkins security group"
  vpc_id      = "${var.vpc_id}"
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }

  
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }

  ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }

  #allow all outgoing traffic 
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.jenkins_sg_name}"
    ByTerraform = "true"
  }
}


resource "aws_security_group" "docker_security_group" {
  name = "${var.docker_sg_name}"
  description = "icdc docker security group"
  vpc_id      = "${var.vpc_id}"

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  
   ingress {
    from_port   =   2375
    to_port     =   2375
    protocol    =   "tcp"
    cidr_blocks =   ["${var.vpc_cidr}"]
  }
  ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
    cidr_blocks = ["${var.public_subnet_a}","${var.public_subnet_c}"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.docker_sg_name}"
    ByTerraform = "true"
  }
}


resource "aws_security_group" "bastion_security_group" {
  name = "${var.bastion_sg_name}"
  description = "icdc bastion host security group"
  vpc_id      = "${var.vpc_id}"

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  ingress {
    from_port           = 0
    to_port             = 0
    protocol            = "-1"
    cidr_blocks         = ["${var.vpc_cidr}"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.bastion_sg_name}"
    ByTerraform = "true"
  }
}


resource "aws_security_group" "alb_security_group" {
  name          =  "${var.alb_sg_name}"   
  vpc_id        =  "${var.vpc_id}"
  

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
    egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
resource "aws_security_group_rule" "app_sg_allow_neo4j_http_db" {
    type = "egress"
    from_port = 7474
    to_port = 7474
    protocol = "tcp"
    security_group_id = "${aws_security_group.db_security_group.id}"
    source_security_group_id = "${aws_security_group.app_security_group.id}"
}
resource "aws_security_group_rule" "db_sg_allow_neo4j_http_app" {
  # Place holder for neo4j
    type = "ingress"
    from_port = 7474
    to_port   = 7474
    protocol  = "tcp"
    security_group_id = "${aws_security_group.app_security_group.id}"
    source_security_group_id = "${aws_security_group.db_security_group.id}"
}
resource "aws_security_group_rule" "app_sg_allow_neo4j_https_db" {
    type = "egress"
    from_port = 7473
    to_port = 7473
    protocol = "tcp"
    security_group_id = "${aws_security_group.db_security_group.id}"
    source_security_group_id = "${aws_security_group.app_security_group.id}"
}
resource "aws_security_group_rule" "db_sg_allow_neo4j_https_app" {
  # Place holder for neo4j
    type = "ingress"
    from_port = 7473
    to_port   = 7473
    protocol  = "tcp"
    security_group_id = "${aws_security_group.app_security_group.id}"
    source_security_group_id = "${aws_security_group.db_security_group.id}"
}
resource "aws_security_group_rule" "app_sg_allow_neo4j_bolts_db" {
    type = "egress"
    from_port = 7687
    to_port = 7687
    protocol = "tcp"
    security_group_id = "${aws_security_group.db_security_group.id}"
    source_security_group_id = "${aws_security_group.app_security_group.id}"
}
resource "aws_security_group_rule" "db_sg_allow_neo4j_bolts_app" {
  # Place holder for neo4j
    type = "ingress"
    from_port = 7687
    to_port   = 7687
    protocol  = "tcp"
    security_group_id = "${aws_security_group.app_security_group.id}"
    source_security_group_id = "${aws_security_group.db_security_group.id}"
}

resource "aws_security_group_rule" "app_sg_allow_ssh_docker" {
  # Place holder for neo4j
    type = "ingress"
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    security_group_id = "${aws_security_group.app_security_group.id}"
    source_security_group_id = "${aws_security_group.docker_security_group.id}"
}