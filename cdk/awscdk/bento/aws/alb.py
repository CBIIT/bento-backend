from aws_cdk import core
from aws_cdk import aws_elasticloadbalancingv2 as elbv2

class ALBResources:
  def createResources(self, ns):

    # Create ALB
    self.bentoALB = elbv2.ApplicationLoadBalancer(self, "bento-alb",
        vpc=self.bentoVPC,
        load_balancer_name="{}-alb".format(ns),
        internet_facing=True)