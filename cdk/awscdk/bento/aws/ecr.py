from aws_cdk import aws_ecr as ecr

class ECRResources:
  def createResources(self, ns):

    # ECR Repository
    self.bentoECR = ecr.Repository(self, "bento-ecr",
        repository_name="{}-ecr-repository".format(ns),
        image_scan_on_push=True)

    # ECR Policy
    self.bentoECR.add_to_resource_policy(self.ecrPolicyStatement)