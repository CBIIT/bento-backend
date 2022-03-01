from aws_cdk import aws_ecr as ecr

class ECRResources:
  def createResources(self, ns):

    # ECR Repository
    self.bentoECR = ecr.Repository(self,
        "{}-ecr".format(ns),
        repository_name="{}-ecr".format(ns).lower(),
        image_scan_on_push=True)

    # ECR Policy
    self.bentoECR.add_to_resource_policy(self.ecrPolicyStatement)