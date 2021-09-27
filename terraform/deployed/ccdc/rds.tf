module "rds_instance" {
  source = "cloudposse/rds/aws"
  #namespace                   = var.namespace
  stage                       = "dev"
  name                        = "ccdc-rds"
  #dns_zone_id                 = "Z89FN1IW975KPE"
  host_name                   = "db"
  security_group_ids          = [aws_security_group.database-sg.id]
  ca_cert_identifier          = "rds-ca-2019"
  allowed_cidr_blocks         = [var.rds_vpc_cidr]
  database_name               = var.database_name
  database_user               = var.database_user
  database_password           = var.database_password
  database_port               = var.database_port
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
  subnet_ids                  = ["subnet-07d177a4d9df5cd32", "subnet-09b0c7407416d4730"]
  vpc_id                      = "vpc-08f154f94dc8a0e34"
  #snapshot_identifier         = "rds:production-2015-06-26-06-05"
  auto_minor_version_upgrade  = true
  allow_major_version_upgrade = false
  apply_immediately           = var.apply_immediately
  maintenance_window          = "Mon:03:00-Mon:04:00"
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





/*
module "rds_instance" {
  source             = "../../modules/rds"
  #namespace          = var.namespace
  #stage              = var.stage
  #name               = var.name
  database_name      = var.database_name
  database_user      = var.database_user
  database_password  = var.database_password
  database_port      = var.database_port
  multi_az           = var.multi_az
  storage_type       = var.storage_type
  allocated_storage  = var.allocated_storage
  storage_encrypted  = var.storage_encrypted
  engine             = var.engine
  engine_version     = var.engine_version
  instance_class     = var.instance_class
  db_parameter_group = var.db_parameter_group

  publicly_accessible = var.publicly_accessible
  allowed_cidr_blocks = ["172.16.0.0/16"] #need to see if i need to parameterize
  vpc_id              = var.vpc_id
  subnet_ids          = var.rds_private_subnets
  security_group_ids  = [aws_security_group.database-sg.id]
  apply_immediately   = var.apply_immediately

  identifier = var.identifier

  tags = {}
}
**/