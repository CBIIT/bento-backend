from aws_cdk import core
from aws_cdk import aws_elasticloadbalancingv2 as elbv2

class ALBListener:
  def createResources(self, ns):

    # Attach ALB to ECS Service
    health_check = elbv2.HealthCheck(interval=core.Duration.seconds(60),
        path="/",
        timeout=core.Duration.seconds(5))

    listener = self.bentoALB.add_listener("PublicListener",
        port=80
        )

    frontendtarget = listener.add_targets("ECS-frontend-Target",
        port=80,
        targets=[self.frontendService],
        health_check=health_check)

    backendtarget = listener.add_targets("ECS-backend-Target",
        port=8080,
        targets=[self.backendService],
        health_check=health_check)

    # Add a fixed error message when browsing an invalid URL
    listener.add_action("ECS-Content-Not-Found",
        action=elbv2.ListenerAction.fixed_response(200,
            message_body="The requested resource is not available")
        )

    elbv2.ApplicationListenerRule(self, id="alb_frontend_rule",
        path_pattern="/*",
        priority=1,
        listener=listener,
        target_groups=[frontendtarget])

    elbv2.ApplicationListenerRule(self, id="alb_backend_rule",
        path_pattern="/v1/graphql/*",
        priority=2,
        listener=listener,
        target_groups=[backendtarget])