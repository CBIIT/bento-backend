from aws_cdk import aws_route53 as route53
from aws_cdk import aws_route53_targets as targets

class Route53Resources:
  def createResources(self, ns):

    # Get Hosted Zone
    hostedZone = route53.HostedZone.from_lookup(self,
        'Bento-Hosted-Zone',
        domain_name=self.config[ns]['domain_name'])

    route53.ARecord(self,
        'Bento-Alias-Record',
        record_name=ns,
        target=route53.RecordTarget.from_alias(targets.LoadBalancerTarget(self.bentoALB)),
        zone=hostedZone)