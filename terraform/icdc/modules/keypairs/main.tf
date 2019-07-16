#import public key for icdc_devops store in the terraform state
resource "secret_resource" "devops_ssh_key_public" {}

locals {
  icdc_devops_pub = "${secret_resource.devops_ssh_key_public.value}"
}
#create aws keypair named devops
resource "aws_key_pair" "keypair" {
  key_name   = "${var.keyname}"
  public_key = "${local.icdc_devops_pub}"
}
