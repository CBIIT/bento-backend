locals {
  db_port = var.db_engine_type == "aurora-postgresql" ? 5432 : 3306
  protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
  any = "-1"
  rds_master_password = {
    password = random_password.master_password.result
  }
  snapshot_name = "${var.stack_name}-${var.env}-${random_id.snapshot.hex}"
}

resource "aws_rds_cluster" "rds" {
  cluster_identifier = "${var.stack_name}-aurora-${var.env}"
  engine                              =  var.db_engine_type
  engine_version                      =  var.db_engine_version
  engine_mode                         =  var.db_engine_mode
  database_name                       =  var.stack_name
  master_username                     =  var.master_username
  master_password                     =  random_password.master_password.result
  final_snapshot_identifier           =  local.snapshot_name
  skip_final_snapshot                 =  var.skip_final_snapshot
  backup_retention_period             =  var.backup_retention_period
  preferred_backup_window             =  var.backup_window
  preferred_maintenance_window        =  var.maintenance_window
  port                                =  local.db_port
  storage_encrypted                   =  var.storage_encrypted
  allow_major_version_upgrade         =  var.allow_major_version_upgrade
  enabled_cloudwatch_logs_exports     =  var.enabled_cloudwatch_logs_exports
  deletion_protection                 =  var.deletion_protection
  db_subnet_group_name                =  aws_db_subnet_group.subnet_group.name
  serverlessv2_scaling_configuration {
    max_capacity =  var.max_capacity
    min_capacity =  var.min_capacity
  }

  tags = var.tags

}


resource "aws_rds_cluster_instance" "instance" {
  cluster_identifier = aws_rds_cluster.rds.cluster_identifier
  instance_class     = var.db_instance_class
  engine             = aws_rds_cluster.rds.engine
  engine_version     = aws_rds_cluster.rds.engine_version
  tags               = var.tags
}

resource "random_password" "master_password" {
  length  = var.master_password_length
  special = false
  keepers = {
    Name =  var.master_username
  }
}

resource "aws_db_subnet_group" "subnet_group" {
  name       = "${var.stack_name}-${var.env}-rds-aurora-subnet-group"
  subnet_ids = var.db_subnet_ids
  tags = var.tags
}

resource "aws_security_group" "rds" {
  name  =  "${var.stack_name}-${var.env}-rds-aurora-sg"
  vpc_id      = var.vpc_id
  description = "Allow traffic to/from RDS Aurora"
  tags = var.tags
}

resource "aws_security_group_rule" "rds_inbound" {
  description = "From allowed SGs"
  type                     = "ingress"
  from_port                = local.db_port
  to_port                  = local.db_port
  protocol                 = local.protocol
  cidr_blocks              = var.allowed_ip_blocks
  security_group_id        = aws_security_group.rds.id
}

resource "random_id" "snapshot" {
  byte_length = 3
  keepers = {
    Name = var.stack_name
  }
}
resource "aws_security_group_rule" "egress" {
  description       = "allow all outgoing traffic"
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = local.any
  cidr_blocks       = local.all_ips
  security_group_id =  aws_security_group.rds.id
}

resource "aws_secretsmanager_secret" "db_secret" {
  name = "${var.stack_name}/rds/aurora/${var.env}"
  recovery_window_in_days = var.secret_recovery_window_in_days
}

resource "aws_secretsmanager_secret_version" "secret_version" {
  secret_id = aws_secretsmanager_secret.db_secret.id
  secret_string = jsonencode(local.rds_master_password)
}