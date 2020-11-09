# data "aws_eip" "icdc_jenkins" {
#   tags = {
#     Name = "icdc_jenkins"
#   }
# }
# data "aws_eip" "icdc_docker" {
#   tags = {
#     Name = "icdc_docker"
#   }
# }
# data "aws_eip" "icdc_k9dc" {
#   tags = {
#     Name = "icdc_k9dc"
#   }
# }

# data "aws_eip" "icdc_neo4j" {
#   tags = {
#     Name = "icdc_neo4j"
#   }
# }

data "aws_eip" "icdc_bastion" {
  tags = {
    Name = "icdc_nat_bastion"
  }
}

