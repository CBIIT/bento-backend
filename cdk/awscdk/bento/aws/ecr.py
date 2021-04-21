from aws_cdk import aws_ecr as ecr

class ECRResources:
  def createResources(self, ns):

    # ECR Repository
    self.bentoECR = ecr.Repository(self, "bento-ecr", repository_name="{}-ecr-repository".format(ns), image_scan_on_push=True)

    # ECR Policy
    #bentoECRPolicy = aws.EcrRepositoryPolicy(self, "bento-ecr-policy", policy=self.ecrPolicy.name, repository=bentoECR.name)