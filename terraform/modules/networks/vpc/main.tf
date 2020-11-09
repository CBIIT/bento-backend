#create vpc
resource "aws_vpc" "vpc" {
  cidr_block = var.vpc_cidr_block
  enable_dns_hostnames = var.enable_hostname_dns
  enable_dns_support = var.enable_dns_support
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"vpc")
  },
  var.tags,
  var.custom_vpc_tags,
  )
}
#create internet gateway
resource "aws_internet_gateway" "igw" {
  #create igw only if a new vpc is to be created and a public subnet is specified
  count = var.create_vpc && length(var.public_subnets) > 0 ? 1 : 0
  vpc_id = aws_vpc.vpc.id
  tags = merge(
  {
    "Name" = format("%s",var.stack_name)
  },
  var.tags,
  var.custom_nat_gateway_tags,
  )

}
#create public route table
resource "aws_route_table" "public_route_table" {
  #create public route table only if a new vpc is to be created and a public subnet is specified
  count = var.create_vpc && length(var.public_subnets) > 0 ? 1 : 0
  vpc_id = aws_vpc.vpc.id
  tags = merge(
  {
    "Name" = format("%s-public",var.stack_name)
  },
  var.tags,
  var.custom_public_route_table_tags,
  )
}

#create public route
resource "aws_route" "public_route_internet" {
  #create public route table only if a new vpc is to be created and a public subnet is specified
  count = var.create_vpc && length(var.public_subnets) > 0 ? 1 : 0
  route_table_id = aws_route_table.public_route_table[count.index].id
  destination_cidr_block = local.all_ips
  gateway_id = aws_internet_gateway.igw[count.index].id

  timeouts {
    create = "5m"
  }
}
#create private route table
resource "aws_route_table" "private_route_table" {
  count = var.create_vpc && local.max_subnet_length > 0 ? local.num_of_nat_gateway : 0
  vpc_id = aws_vpc.vpc.id
  tags = merge(
  {
    "Name" = var.single_nat_gateway ? "${var.stack_name}-private" : format("%s-private-%s",var.stack_name,element(var.availability_zones,count.index )),
  },
  var.tags,
  var.custom_private_route_table_tags,
  )
}

#create database route table
resource "aws_route_table" "database_route_table" {
  count = var.create_vpc && var.create_db_subnet_group && length(var.private_subnets) > 0 ? 1 : 0
  vpc_id = aws_vpc.vpc.id
  tags = merge(
  {
    "Name" = var.single_nat_gateway ? "${var.stack_name}-private" : format("%s-private-%s",var.stack_name,element(var.availability_zones,count.index )),
  },
  var.tags,
  var.custom_db_subnet_group_tags,
  )
}

#create public subnets
resource "aws_subnet" "public_subnet" {
  #use the number of subnets provided
  count = var.create_vpc && length(var.public_subnets) > 0 && (!var.one_nat_gateway_per_az || length(var.public_subnets) >= length(var.availability_zones)) ? length(var.public_subnets) : 0
  cidr_block = var.public_subnets[count.index]
  availability_zone = element(var.availability_zones,count.index )
  vpc_id = aws_vpc.vpc.id

  tags = merge(
  {
    "Name" = format("%s-public-%s",var.stack_name,element(var.availability_zones,count.index )),
  },
  var.tags,
  var.custom_public_subnet_tags,
  )
}
#create private subnets
resource "aws_subnet" "private_subnet" {
  #create private subnets provided
  count = var.create_vpc && length(var.private_subnets) > 0 ? length(var.private_subnets) : 0
  cidr_block = var.private_subnets[count.index]
  availability_zone = element(var.availability_zones,count.index )
  vpc_id = aws_vpc.vpc.id

  tags = merge(
  {
    "Name" = format("%s-private-%s",var.stack_name,element(var.availability_zones,count.index )),
  },
  var.tags,
  var.custom_private_subnet_tags,
  )
}
#create database subnet
resource "aws_subnet" "db_subnet" {
  #create db subnets provided
  count = var.create_vpc && length(var.db_subnets) > 0 ? length(var.db_subnets) : 0
  cidr_block = var.db_subnets[count.index]
  availability_zone = element(var.availability_zones,count.index )
  vpc_id = aws_vpc.vpc.id

  tags = merge(
  {
    "Name" = format("%s-database-%s",var.stack_name,element(var.availability_zones,count.index )),
  },
  var.tags,
  var.custom_db_subnet_tags,
  )
}
#create db subnet group to use with managed service like RDS
resource "aws_db_subnet_group" "db_subnet_group" {
  count = var.create_vpc && length(var.db_subnets) > 0 && var.create_db_subnet_group ? 1 : 0
  name = "${var.stack_name}-${var.env}-${var.name_db_subnet_group}"
  description = "${var.stack_name}-${var.env} database subnet group"
  subnet_ids = aws_subnet.db_subnet.*.id
  tags = merge(
  {
    "Name" = format("%s",var.stack_name),
  },
  var.tags,
  var.custom_db_subnet_tags,
  )
}

resource "aws_eip" "nat" {
  count = var.create_vpc && (var.enable_nat_gateway && !var.reuse_nat_ips) ? local.num_of_nat_gateway : 0
  vpc = true
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,element(var.availability_zones,var.single_nat_gateway ? 0 : count.index )),
  },
  var.tags,
  var.custom_nat_gateway_tags,
  )
}

resource "aws_nat_gateway" "aws_nat" {
  count = var.create_vpc && var.enable_nat_gateway ? local.num_of_nat_gateway : 0
  allocation_id = element(local.nat_gateway_ips,(var.single_nat_gateway ? 0 : count.index))
  subnet_id = element(aws_subnet.public_subnet.*.id,(var.single_nat_gateway ? 0 : count.index) )
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,element(var.availability_zones,var.single_nat_gateway ? 0 : count.index )),
  },
  var.tags,
  var.custom_nat_gateway_tags,
  var.custom_nat_gateway_tags,
  )
  depends_on = [aws_internet_gateway.igw]
}

resource "aws_route" "private_route_nat_gateway" {
  count = var.create_vpc && var.enable_nat_gateway ? local.num_of_nat_gateway : 0

  route_table_id = element(aws_route_table.private_route_table.*.id,count.index )
  destination_cidr_block = local.all_ips
  nat_gateway_id = element(aws_nat_gateway.aws_nat.*.id,count.index )
  timeouts {
    create = "5m"
  }
}
resource "aws_route" "database_route_nat_gateway" {
  count = var.create_vpc && var.create_db_subnet_group && length(var.db_subnets) > 0 ? 1 : 0
  route_table_id         = aws_route_table.database_route_table[0].id
  destination_cidr_block = local.all_ips
  nat_gateway_id = element(aws_nat_gateway.aws_nat.*.id,count.index )

  timeouts {
    create = "5m"
  }

}
locals {
  all_ips = "0.0.0.0/0"
  max_subnet_length = max(length(var.private_subnets),length(var.db_subnets))
  num_of_nat_gateway = var.single_nat_gateway ? 1 : var.one_nat_gateway_per_az ? length(var.availability_zones) : local.max_subnet_length
  nat_gateway_ips = split(",", (var.reuse_nat_ips ? join(",", var.external_nat_ip_ids) : join(",", aws_eip.nat.*.id)))
}
#route table association
resource "aws_route_table_association" "private-subnet" {
  count = var.create_vpc && length(var.private_subnets) > 0 ? length(var.private_subnets) : 0
  route_table_id = element(aws_route_table.private_route_table.*.id,var.single_nat_gateway ? 0 : count.index )
  subnet_id = element(aws_subnet.private_subnet.*.id,count.index)
}
resource "aws_route_table_association" "public-subnet" {
  count = var.create_vpc && length(var.public_subnets) > 0 ? length(var.public_subnets) : 0
  route_table_id = aws_route_table.public_route_table[0].id
  subnet_id = element(aws_subnet.public_subnet.*.id,count.index)
}

resource "aws_route_table_association" "db-subnet" {
  count = var.create_vpc && length(var.db_subnets) > 0 ? length(var.db_subnets) : 0
  route_table_id = element(coalescelist(aws_route_table.database_route_table.*.id,aws_route_table.private_route_table.*.id),var.single_nat_gateway || var.create_db_subnet_group ? 0 : count.index)
  subnet_id = element(aws_subnet.db_subnet.*.id,count.index)
}