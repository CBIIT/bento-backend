
resource "aws_instance" "match" {
    count           = "${var.count}"
    ami             = "${var.ami_id}"
    instance_type   = "${var.instance_type}"
    subnet_id       = "${var.subnet_id}"

    tags {
        Name        = "icdc match"
        Terraform   = true
    }
}
