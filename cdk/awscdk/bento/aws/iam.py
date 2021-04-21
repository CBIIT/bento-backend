from aws_cdk import aws_iam as iam

class IAMResources:
  def createResources(self, ns):

    # ECS Instance Role
    self.ecsInstanceRole = iam.Role(self, "ecs-instance-role", role_name="{}-ecs-instance-role".format(ns), assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"))
    
    # ECS Service Role
    self.ecsServiceRole = iam.Role(self, "ecs-service-role", role_name="{}-ecs-service-role".format(ns), assumed_by=iam.ServicePrincipal("ecs.amazonaws.com"))
    
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
    
    ssmPolicy = iam.Policy(self, "ssm-policy", policy_name="{}-ssm-policy".format(ns), document=iam.PolicyDocument.from_json(ssmPolicyDocument))
    self.ecsInstanceRole.attach_inline_policy(ssmPolicy)
    
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
    
    ec2Policy = iam.Policy(self, "ec2-policy", policy_name="{}-ec2-policy".format(ns), document=iam.PolicyDocument.from_json(ec2PolicyDocument))
    self.ecsInstanceRole.attach_inline_policy(ec2Policy)
    
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

    ecsPolicy = iam.Policy(self, "ecs-policy", policy_name="{}-ecs-policy".format(ns), document=iam.PolicyDocument.from_json(ecsPolicyDocument))
    self.ecsInstanceRole.attach_inline_policy(ecsPolicy)
    
#    # ECR Policy
#    ecrPolicyDocument = {
#      "Version": "2012-10-17",
#      "Statement": [
#        {
#          "Sid": "ElasticContainerRegistryPushAndPull",
#          "Effect": "Allow",
#          "Principal": {
#            "AWS": [
#              "local.my_account"
#            ],
#          },
#          "Action": [
#            "ecr:GetDownloadUrlForLayer",
#            "ecr:BatchGetImage",
#            "ecr:BatchCheckLayerAvailability",
#            "ecr:PutImage",
#            "ecr:InitiateLayerUpload",
#            "ecr:UploadLayerPart",
#            "ecr:CompleteLayerUpload"
#          ]
#        }
#      ]
#    }
#    
#    self.ecrPolicy = aws.IamPolicy(self, "ecr-policy", name="{}-ecr-policy".format(ns), path="/", policy=json.dumps(ecrPolicyDocument))