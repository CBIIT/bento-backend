provider "aws" {
  region = var.region
  profile = var.profile
}
provider "aws" {
  alias = "accepter"
  region = var.region
  profile = var.profile
}

terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/vpc-peering/terraform.tfstate"
    region = "us-east-1"
    workspace_key_prefix = "env"
    encrypt = true
  }
}

resource "aws_vpc_peering_connection" "management" {
  peer_owner_id = data.aws_caller_identity.account.account_id
  vpc_id   = data.aws_vpc.management.id
  peer_vpc_id        = data.aws_vpc.other_vpc.id
  auto_accept   = true
  tags = merge(
  {
      "Name" = format("%s-%s",var.stack_name,"vpc-peering")
  },
  var.tags,
  )
  accepter {
    allow_remote_vpc_dns_resolution = true
  }

  requester {
    allow_remote_vpc_dns_resolution = true
  }
}

//resource "aws_vpc_peering_connection_accepter" "other" {
//  vpc_peering_connection_id = aws_vpc_peering_connection.management.id
//  auto_accept   = true
//  tags = merge(
//  {
//    "Name" = format("%s-%s",var.stack_name,"vpc-peering")
//  },
//  var.tags,
//  )
//
//}

resource "aws_route" "bento-mgt" {
  for_each = data.aws_route_tables.management.ids
  //  route_table_id            = data.aws_route_tables.sandbox.ids[count.index]
  route_table_id = each.value
  destination_cidr_block    = data.aws_vpc.other_vpc.cidr_block
  vpc_peering_connection_id = aws_vpc_peering_connection.management.id
}

resource "aws_route" "other_vpc_route" {
  for_each = data.aws_route_tables.others.ids
//  route_table_id            = data.aws_route_tables.bento.ids[count.index]
  route_table_id = each.value
  destination_cidr_block    = data.aws_vpc.management.cidr_block
  vpc_peering_connection_id = aws_vpc_peering_connection.management.id
}


