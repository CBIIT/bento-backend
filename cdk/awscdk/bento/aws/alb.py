from aws_cdk import core
from aws_cdk import aws_elasticloadbalancingv2 as elbv2

class ALBResources:
  def createResources(self, ns):

    # Create ALB
    self.bentoALB = elbv2.ApplicationLoadBalancer(self, "bento-alb",
        vpc=self.bentoVPC,
        internet_facing=True)

    health_check = elbv2.HealthCheck(interval=core.Duration.seconds(60),
        path="/",
        timeout=core.Duration.seconds(5))

    # Attach ALB to ECS Service
    listener = self.bentoALB.add_listener("PublicListener",
        port=80,
        open=True)
    
    albtarget = listener.add_targets("ECS-Target",
        port=80,
        targets=[self.frontendService],
        health_check=health_check)
    
    albrule = elbv2.ApplicationListenerRule(self, id="alb listener rule",
        path_pattern="/",
        priority=1,
        listener=listener,
        target_groups=[albtarget])