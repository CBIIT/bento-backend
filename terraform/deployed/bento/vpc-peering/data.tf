data "aws_caller_identity" "account" {

}
data "aws_vpc" "management" {
  filter {
    name = "tag:Name"
    values = [join("-",[var.stack_name,var.management_environment,"vpc"])]
  }
}

data "aws_vpc" "other_vpc" {
  filter {
    name = "tag:Name"
    values = [join("-",[var.stack_name,var.env,"vpc"])]
  }

}

data "aws_route_tables" "management" {
//  provider = aws.accepter
  vpc_id = data.aws_vpc.management.id
}

data "aws_route_tables" "others" {
  vpc_id = data.aws_vpc.other_vpc.id
}

