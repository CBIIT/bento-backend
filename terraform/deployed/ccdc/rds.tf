module "rds_instance" {
  source = "cloudposse/rds/aws"
  name                        =  "${var.stack_name}-${var.env}-rds"
  security_group_ids          = [aws_security_group.rds_sg.id]
  allowed_cidr_blocks         = [data.terraform_remote_state.network.outputs.vpc_cidr_block]
  database_name               = var.database_name
  database_user               = var.database_user
  database_password           = random_password.rds_password.result
  database_port               = local.rds_port
  multi_az                    = var.multi_az
  storage_type                = var.storage_type
  allocated_storage           = var.allocated_storage
  storage_encrypted           = var.storage_encrypted
  engine                      = var.engine
  engine_version              = var.engine_version
  major_engine_version        = "8.0"
  instance_class              = var.instance_class
  db_parameter_group          = "mysql8.0"
  publicly_accessible         = var.publicly_accessible
  subnet_ids                  = data.terraform_remote_state.network.outputs.private_subnets_ids
  vpc_id                      = data.terraform_remote_state.network.outputs.vpc_id
  auto_minor_version_upgrade  = true
  allow_major_version_upgrade = false
  apply_immediately           = var.apply_immediately
  skip_final_snapshot         = false
  copy_tags_to_snapshot       = true
  backup_retention_period     = 7
  backup_window               = "22:00-03:00"

  /*
  db_parameter = [
    { name  = "myisam_sort_buffer_size", value = "1048576" },
    { name  = "sort_buffer_size", value = "2097152" }
  ]

  db_options = [
    { option_name = "MARIADB_AUDIT_PLUGIN"
      option_settings = [
        { name = "SERVER_AUDIT_EVENTS", value = "CONNECT" },
        { name = "SERVER_AUDIT_FILE_ROTATIONS", value = "37" }
      ]
    }
  ]
*/
}

#create admin password for rds
resource "random_password" "rds_password" {
  length           = 12
  special          = true
  override_special = "_%@"
  keepers = {
    keep = true
  }
}


#create rds security group
resource "aws_security_group" "rds_sg" {
  name = "${var.stack_name}-${var.env}-rds-sg"
  description = "rds security group"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"rds-sg")
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "allow_backend" {
  from_port = local.rds_port
  protocol = local.tcp_protocol
  to_port = local.rds_port
  cidr_blocks = flatten([data.terraform_remote_state.network.outputs.private_subnets])
  security_group_id = aws_security_group.rds_sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.rds_sg.id
  type = "egress"
}
