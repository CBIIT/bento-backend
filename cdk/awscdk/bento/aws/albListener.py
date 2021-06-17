from aws_cdk import core
from aws_cdk import aws_elasticloadbalancingv2 as elbv2

class ALBListener:
  def createResources(self, ns):

    # Attach ALB to ECS Service
    health_check = elbv2.HealthCheck(interval=core.Duration.seconds(60),
        path="/",
        timeout=core.Duration.seconds(5))

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