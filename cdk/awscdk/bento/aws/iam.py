from aws_cdk import core
from aws_cdk import aws_iam as iam

class IAMResources:
  def createResources(self, ns):

    # ECS Instance Role
    self.ecsInstanceRole = iam.Role(self,
        "{}-ecs-instance-role".format(ns),
        role_name="{}-ecs-instance-role".format(ns),
        assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"))
    core.Tags.of(self.ecsInstanceRole).add("Name", "{}-ecs-instance-role".format(ns))
    
    # ECS Service Role
    self.ecsServiceRole = iam.Role(self,
        "{}-ecs-service-role".format(ns),
        role_name="{}-ecs-service-role".format(ns),
        assumed_by=iam.ServicePrincipal("ecs.amazonaws.com"))
    core.Tags.of(self.ecsServiceRole).add("Name", "{}-ecs-service-role".format(ns))
    
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
    
    ssmPolicy = iam.Policy(self,
        "{}-ssm-policy".format(ns),
        policy_name="{}-ssm-policy".format(ns),
        document=iam.PolicyDocument.from_json(ssmPolicyDocument))
    core.Tags.of(ssmPolicy).add("Name", "{}-ssm-policy".format(ns))
        
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
    
    ec2Policy = iam.Policy(self,
        "{}-ec2-policy".format(ns),
        policy_name="{}-ec2-policy".format(ns),
        document=iam.PolicyDocument.from_json(ec2PolicyDocument))
    core.Tags.of(ec2Policy).add("Name", "{}-ec2-policy".format(ns))
    
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

    ecsPolicy = iam.Policy(self,
        "{}-ecs-policy".format(ns),
        policy_name="{}-ecs-policy".format(ns),
        document=iam.PolicyDocument.from_json(ecsPolicyDocument))
    core.Tags.of(ecsPolicy).add("Name", "{}-ecs-policy".format(ns))
    
    self.ecsInstanceRole.attach_inline_policy(ecsPolicy)
    self.ecsInstanceRole.add_managed_policy(iam.ManagedPolicy. from_aws_managed_policy_name('service-role/AmazonEC2ContainerServiceforEC2Role'))
    
    # ECR
    self.ecrPolicyStatement = iam.PolicyStatement(
            sid="ElasticContainerRegistryPushAndPull",
            effect=iam.Effect.ALLOW,
            actions=["ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:BatchCheckLayerAvailability",
                "ecr:PutImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload"],
            principals=[iam.AccountRootPrincipal()])