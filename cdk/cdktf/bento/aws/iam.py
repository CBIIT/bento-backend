import json
import imports.aws as aws

class IAMResources:
  def createResources(self, ns, bentoTags):

    # IAM
    # ECS Instance
    ecsInstancePolicyDoc = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Action": [ "sts:AssumeRole" ],
          "Effect": "Allow",
          "Principal": {
            "Service": [
              "ec2.amazonaws.com"
            ]
          }
        }
      ]
    }
    
    self.ecsInstanceRole = aws.IamRole(self, "ecs-instance-role", name="{}-ecs-instance-role".format(ns), assume_role_policy=json.dumps(ecsInstancePolicyDoc), tags=bentoTags)
    self.ecsInstanceRolePolicy = aws.IamRolePolicyAttachment(self, "ecs-instance-role-policy", role=self.ecsInstanceRole.name, policy_arn="arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role")
    self.ecsInstanceProfile = aws.IamInstanceProfile(self, "ecs-instance-profile", name="{}-ecs-instance-profile".format(ns), path="/", role=self.ecsInstanceRole.id)

    # ECS Service
    ecsServicePolicyDoc = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Action": [ "sts:AssumeRole" ],
          "Effect": "Allow",
          "Principal": {
            "Service": [
              "ecs.amazonaws.com"
            ]
          }
        }
      ]
    }
    
    self.ecsServiceRole = aws.IamRole(self, "ecs-service-role", name="{}-ecs-service-role".format(ns), assume_role_policy=json.dumps(ecsServicePolicyDoc), tags=bentoTags)
    self.ecsServiceRolePolicy = aws.IamRolePolicyAttachment(self, "ecs-service-role-policy", role=self.ecsServiceRole.name, policy_arn="arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceRole")

    # ECR Policy
    ecrPolicyDocument = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Sid": "ElasticContainerRegistryPushAndPull",
          "Effect": "Allow",
          "Principal": {
            "AWS": [
              "local.my_account"
            ],
          },
          "Action": [
            "ecr:GetDownloadUrlForLayer",
            "ecr:BatchGetImage",
            "ecr:BatchCheckLayerAvailability",
            "ecr:PutImage",
            "ecr:InitiateLayerUpload",
            "ecr:UploadLayerPart",
            "ecr:CompleteLayerUpload"
          ]
        }
      ]
    }
    
    self.ecrPolicy = aws.IamPolicy(self, "ecr-policy", name="{}-ecr-policy".format(ns), path="/", policy=json.dumps(ecrPolicyDocument))

    # SSM
    ssmPolicyDocument = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Sid": "10",
          "Effect": "Allow",
          "Action": [
            "cloudwatch:PutMetricData",
            "ds:CreateComputer",
            "ds:DescribeDirectories",
            "ec2:DescribeInstanceStatus",
            "logs:*",
            "ssm:*",
            "ec2messages:*"
          ],
          "Resource": "*"
        },
        {
          "Sid": "",
          "Effect": "Allow",
          "Action": "iam:CreateServiceLinkedRole",
          "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*",
          "Condition": {
            "StringLike": {
              "iam:AWSServiceName": "ssm.amazonaws.com"
            }
          }
        },
        {
          "Sid": "",
          "Effect": "Allow",
          "Action": [
            "iam:DeleteServiceLinkedRole",
            "iam:GetServiceLinkedRoleDeletionStatus"
          ],
          "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*"
        }
      ]
    }
    
    self.ssmPolicy = aws.IamPolicy(self, "ssm-policy", name="{}-ssm-policy".format(ns), path="/", policy=json.dumps(ssmPolicyDocument))
    self.ecsInstanceRoleSSMPolicy = aws.IamRolePolicyAttachment(self, "ecs-instance-role-ssm-policy", role=self.ecsInstanceRole.name, policy_arn=self.ssmPolicy.arn)
    
    # EC2
    ec2PolicyDocument = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Action": "ec2:*",
          "Resource": "*"
        }
      ]
    }
    
    self.ec2Policy = aws.IamPolicy(self, "ec2-policy", name="{}-ec2-policy".format(ns), path="/", policy=json.dumps(ec2PolicyDocument))
    self.ecsInstanceRoleEC2Policy = aws.IamRolePolicyAttachment(self, "ecs-instance-role-ec2-policy", role=self.ecsInstanceRole.name, policy_arn=self.ec2Policy.arn)
    
    # ECS
    ecsPolicyDocument = {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Action": "ecs:*",
          "Resource": "*"
        },
        {
          "Effect": "Allow",
          "Action": "ecr:*",
          "Resource": "*"
        },
        {
          "Effect": "Allow",
          "Action": "ssm:*",
          "Resource": "*"
        },
        {
          "Effect": "Allow",
          "Action": "s3:*",
          "Resource": "*"
        }
      ]
    }

    self.ecsPolicy = aws.IamPolicy(self, "ecs-policy", name="{}-ecs-policy".format(ns), path="/", policy=json.dumps(ecsPolicyDocument))
    self.ecsInstanceRoleECSPolicy = aws.IamRolePolicyAttachment(self, "ecs-instance-role-ecs-policy", role=self.ecsInstanceRole.name, policy_arn=self.ecsPolicy.arn)