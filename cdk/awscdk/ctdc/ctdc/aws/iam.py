from aws_cdk import core
from aws_cdk import aws_iam as iam

class IAMResources:
  def createResources(self, ns):

    # Opensearch
    self.osPolicyStatement = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            actions=["es:*"],
            principals=[iam.AnyPrincipal()],
            resources=["arn:aws:es:{}:{}:domain/{}-es/*".format(core.Stack.of(self).region, core.Stack.of(self).account, ns)])