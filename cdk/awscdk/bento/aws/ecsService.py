from aws_cdk import aws_ecs as ecs
from aws_cdk import aws_ec2 as ec2

class ECSService:
  def createResources(self, ns):

    # Security Group Updates
    albsg = self.bentoALB.connections.security_groups[0]
    ecsasg = self.bentoECS_ASG.connections.security_groups[0]

    ecsasg.add_ingress_rule(
        albsg,
        ec2.Port.tcp(8080)
    )
    ecsasg.add_ingress_rule(
        albsg,
        ec2.Port.tcp(80)
    )

    # Backend Task Definition
    backendECSTask = ecs.Ec2TaskDefinition(self, "bento-ecs-backend",
        network_mode=ecs.NetworkMode.AWS_VPC)
    
    backendECSContainer = backendECSTask.add_container('api',
        image = ecs.ContainerImage.from_registry("cbiitssrepo/bento-backend:latest"),
        memory_reservation_mib=1024,
        cpu=512)
    
    backend_port_mapping = ecs.PortMapping(
        container_port=8080,
        host_port=8080,
        protocol=ecs.Protocol.TCP
    )
    
    backendECSContainer.add_port_mappings(backend_port_mapping)

    # Backend Service
    self.backendService = ecs.Ec2Service(self, "{}-backend".format(ns),
        task_definition=backendECSTask,
        cluster=self.bentoECS)

    # Frontend Task Definition
    frontendECSTask = ecs.Ec2TaskDefinition(self, "bento-ecs-frontend",
        network_mode=ecs.NetworkMode.AWS_VPC)
    
    frontendECSContainer = frontendECSTask.add_container('ui',
        image = ecs.ContainerImage.from_registry("cbiitssrepo/bento-frontend:latest"),
        memory_reservation_mib=1024,
        cpu=512)

    frontend_port_mapping = ecs.PortMapping(
        container_port=80,
        host_port=80,
        protocol=ecs.Protocol.TCP
    )
    
    frontendECSContainer.add_port_mappings(frontend_port_mapping)

    # Frontend Service
    self.frontendService = ecs.Ec2Service(self, "{}-frontend".format(ns),
        task_definition=frontendECSTask,
        cluster=self.bentoECS)