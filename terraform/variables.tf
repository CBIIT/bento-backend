
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}

variable "vpc_id" {
  description = "vpc id to to launch the ALB"
  type        = string
}

variable "region" {
  description = "aws region to use for this resource"
  type = string
  default = "us-east-1"
}

variable "enable_version" {
  description = "enable bucket versioning"
  default = false
  type = bool
}
variable "lifecycle_rule" {
  description = "object lifecycle rule"
  type = any
  default = []
}
variable "certificate_domain_name" {
  description = "domain name for the ssl cert"
  type = string
}

variable "cloud_platform" {
  description = "choose cloud platform to use. We have two - leidos or cloudone"
  default = "leidos"
  type = string
}
variable "internal_alb" {
  description = "is this alb internal?"
  default = false
  type = bool
}
variable "lb_type" {
  description = "Type of loadbalancer"
  type = string
  default = "application"
}
variable "aws_account_id" {
  type = map(string)
  description = "aws account to allow for alb s3 logging"
  default = {
    us-east-1 = "127311923021"
  }
}
variable "public_subnet_ids" {
  description = "Provide list of public subnets to use in this VPC. Example 10.0.1.0/24,10.0.2.0/24"
  type = list(string)
}

variable "private_subnet_ids" {
  description = "Provide list private subnets to use in this VPC. Example 10.0.10.0/24,10.0.11.0/24"
  type = list(string)
}

variable "attach_bucket_policy" {
  description = "set to true if you want bucket policy and provide value for policy variable"
  type        = bool
  default     = true
}
variable "microservices" {
  type = map(object({
    name = string
    port = number
    health_check_path = string
    priority_rule_number = number
    image_url = string
    cpu = number
    memory = number
    path = list(string)
    number_container_replicas = number
  }))
}
variable "domain_name" {
  description = "domain name for the application"
  type = string
}

variable "application_subdomain" {
  description = "subdomain of the app"
  type = string
}
variable "s3_force_destroy" {
  description = "force destroy bucket"
  default = true
  type = bool
}
variable "ecr_repo_names" {
  description = "list of repo names"
  type = list(string)
}
variable "create_ecr_repos" {
  type = bool
  default = false
  description = "choose whether to create ecr repos or not"
}
variable "create_opensearch_cluster" {
  description = "choose to create opensearch cluster or not"
  type = bool
  default = false
}
variable "opensearch_ebs_volume_size" {
  description = "size of the ebs volume attached to the opensearch instance"
  type = number
  default = 200
}
variable "opensearch_instance_type" {
  description = "type of instance to be used to create the elasticsearch cluster"
  type = string
  default = "t3.medium.elasticsearch"
}
variable "opensearch_version" {
  type = string
  description = "specify es version"
  default = "OpenSearch_1.1"
}
variable "allowed_ip_blocks" {
  description = "allowed ip block for the opensearch"
  type = list(string)
  default = []
}
variable "create_os_service_role" {
  type = bool
  default = false
  description = "change this value to true if running this script for the first time"
}
variable "create_dns_record" {
  description = "choose to create dns record or not"
  type = bool
  default = true
}
variable "bastion_host_security_group_id" {
  description = "security group id of the bastion host"
  type = string
  default = "sg-0c94322085acbfd97"
}
variable "katalon_security_group_id" {
  description = "security group id of the bastion host"
  type = string
  default = "sg-0f07eae0a9b3a0bb8"
}

variable "db_subnet_id" {
  description = "subnet id to launch db"
  type = string
  default = ""
}

variable "db_instance_volume_size" {
  description = "volume size of the instances"
  type = number
  default = 100
}
variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
  default = "bento"
}
variable "db_private_ip" {
  description = "private ip of the db instance"
  type = string
  default = "10.0.0.2"
}
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
  default = "devops"
}
variable "public_ssh_key_ssm_parameter_name" {
  description = "name of the ssm parameter holding ssh key content"
  default = "ssh_public_key"
  type = string
}
variable "create_db_instance" {
  description = "set this value if you want create db instance"
  default = false
  type = bool
}

variable "multi_az_enabled" {
  description = "set to true to enable multi-az deployment"
  type        = bool
  default = false
}
variable "opensearch_instance_count" {
  description = "the number of data nodes to provision for each instance in the cluster"
  type        = number
  default     = 1
}
variable "automated_snapshot_start_hour" {
  description = "hour when automated snapshot to be taken"
  type        = number
  default     = 23
}
variable "create_cloudwatch_log_policy" {
  description = "Due cloudwatch log policy limits, this should be option, we can use an existing policy"
  default = false
  type = bool
}
variable "create_env_specific_repo" {
  description = "choose to create environment specific repo. Example bento-dev-frontend"
  type = bool
  default = false
}
variable "database_instance_type" {
  description = "ec2 instance type to use"
  type        = string
  default     = "t3.large"
}
variable "add_opensearch_permission" {
  type = bool
  default = false
  description = "choose to create opensearch permission or not"
}
variable "target_account_cloudone"{
  description = "to add check conditions on whether the resources are brought up in cloudone or not"
  type        = bool
  default =   false
}
variable "iam_prefix" {
  type = string
  default = "power-user"
  description = "nci iam power user prefix"
}
variable "create_instance_profile" {
  type = bool
  default = false
  description = "set to create instance profile"
}
variable "allow_cloudwatch_stream" {
  type = bool
  default = true
  description = "allow cloudwatch stream for the containers"
}
variable "db_subnet_ids" {
  type        = list(string)
  default     = []
  description = "list of subnet IDs to usee"
}
variable "db_engine_mode" {
  type        = string
  default     = "serverless"
  description = "The database engine mode."
}
variable "db_engine_version" {
  description = "aurora database engine version."
  type        = string
  default     = "5.6.10a"
}
variable "db_engine_type" {
  description = "Aurora database engine type"
  type        = string
  default     = "aurora-mysql"
}
variable "db_instance_class" {
  description = "Instance type to use for the db"
  type        = string
  default     = "db.serverless"
}
variable "master_username" {
  description = "username for this db"
  type        = string
  default     = ""
  sensitive   = true
}
variable "database_name" {
  description = "name of the database"
  type = string
  default = "bento"
}
variable "create_aurora_rds" {
  description = "create rds or not"
  type = bool
  default = false
}
variable "create_cloudfront" {
  description = "create cloudfront or not"
  type = bool
  default = false
}
variable "cloudfront_distribution_bucket_name" {
  description = "specify the name of s3 bucket for cloudfront"
  type = string
}
variable "cloudfront_origin_acess_identity_description" {
  description = "description for OAI"
  type = string
  default = "cloudfront origin access identify for s3"
}
variable "cloudfront_log_path_prefix_key" {
  description = "path prefix to where cloudfront send logs to s3 bucket"
  type = string
  default = "cloudfront/logs"
}

variable "alarms" {
  description = "alarms to be configured"
  type = map(map(string))
}

variable "slack_secret_name" {
  type = string
  description = "name of cloudfront slack secret"
}
variable "cloudfront_slack_channel_name" {
  type = string
  description = "cloudfront slack name"
}
variable "create_files_bucket" {
  description = "indicate if you want to create files bucket or use existing one"
  type = bool
  default = false
}