resource "aws_elasticache_subnet_group" "redis_subnet_group" {
  name       = "${var.stack_name}-${var.env}-redis-subnet-group"
  subnet_ids = data.terraform_remote_state.network.outputs.private_subnets_ids
}


resource "aws_elasticache_cluster" "redis_cluster" {
  cluster_id           = "${lower(var.stack_name)}-${var.env}-redis-cluster"
  engine               = "redis"
  node_type            = "cache.t3.medium"
  num_cache_nodes      = 1
//  parameter_group_name = "default.redis6.x.cluster.on"
  engine_version       = "3.2.10"
  snapshot_retention_limit = 5
  snapshot_window          = "00:00-05:00"
  security_group_ids = [aws_security_group.redis.id]
  subnet_group_name = aws_elasticache_subnet_group.redis_subnet_group.name

  port                 = 6379
}

//resource "aws_elasticache_replication_group" "replication_group" {
//  replication_group_id          = "${var.stack_name}-${var.env}-redis-cluster"
//  replication_group_description = "Redis cluster ${var.stack_name} ${var.env}"
//
//  node_type            = "cache.t3.medium"
//  port                 = 6379
//  parameter_group_name = "default.redis6.x.cluster.on"
//
//  snapshot_retention_limit = 5
//  snapshot_window          = "00:00-05:00"
//  security_group_ids = [aws_security_group.redis.id]
//  subnet_group_name = aws_elasticache_subnet_group.redis_subnet_group.name
//
//  automatic_failover_enabled = true
//
//  cluster_mode {
//    replicas_per_node_group = 1
//    num_node_groups         = var.redis_node_group
//  }
//}

resource "aws_security_group" "redis" {
  name = "${var.stack_name}-${var.env}-redis-sg"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-redis-%s-sg",var.stack_name,var.env),
  },
  var.tags,
  )
}
resource "aws_security_group_rule" "test" {
  type        = "ingress"
  from_port   = local.redis
  to_port     = local.redis
  protocol    = local.any_protocol
  cidr_blocks = flatten([data.terraform_remote_state.network.outputs.private_subnets])
  security_group_id = aws_security_group.redis.id
}
resource "aws_security_group_rule" "redis_host_ssh" {
  from_port = local.redis
  protocol = local.any_protocol
  to_port = local.redis
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  security_group_id = aws_security_group.redis.id
  type = "ingress"
}
resource "aws_security_group_rule" "redis_jenkins" {
  from_port = local.redis
  protocol = local.any_protocol
  to_port = local.redis
  source_security_group_id = data.terraform_remote_state.bastion.outputs.jenkins_security_group_id
  security_group_id = aws_security_group.redis.id
  type = "ingress"
}
resource "aws_security_group_rule" "all_outbound_redis" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.redis.id
  type = "egress"
}