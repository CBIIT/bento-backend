
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket  = "icdc-sandbox-terraform-state"
    key     = "state/sandbox/terraform.tfstate"
    encrypt = "true"
    region  = "us-east-1"
  }
}

data "aws_caller_identity" "account_info" {}
data "aws_elb_service_account" "ecs" {}

data "template_file" "ssh_config" {
  template = file("templates/ssh_config.cfg")

  vars = {
    stack_name = var.stack_name
    bastion_ip = data.terraform_remote_state.network.outputs.bastion_public_ip
  }
}

resource "null_resource" "ssh_config" {
  triggers = {
    template_rendered = data.template_file.ssh_config.rendered
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.ssh_config.rendered}' > /tmp/${var.stack_name}_ssh_config"
  }
}

