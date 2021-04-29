from aws_cdk import aws_ecs as ecs
from aws_cdk import aws_ec2 as ec2

class ECSResources:
  def createResources(self, ns):

    # ECS Cluster
    self.bentoECS = ecs.Cluster(self, "bento-ecs", cluster_name="{}".format(ns), vpc=self.bentoVPC)
    self.bentoECS.add_capacity('bento-ecs-instance', instance_type=ec2.InstanceType('t3.medium'), min_capacity=1, max_capacity=1)

    # Backend Task Definition
    backendECSTask = ecs.Ec2TaskDefinition(self, "bento-ecs-backend")
    backendECSContainer = backendECSTask.add_container('api', image = ecs.ContainerImage.from_registry("cbiitssrepo/bento-backend:latest"), memory_reservation_mib=1024, cpu=512)
    backendECSContainer.add_port_mappings({ 'containerPort' : 8080, 'hostPort' : 8080 })

    # Frontend Task Definition
    frontendECSTask = ecs.Ec2TaskDefinition(self, "bento-ecs-frontend")
    frontendECSContainer = frontendECSTask.add_container('ui', image = ecs.ContainerImage.from_registry("cbiitssrepo/bento-frontend:latest"), memory_reservation_mib=1024, cpu=512)
    frontendECSContainer.add_port_mappings({ 'containerPort' : 80, 'hostPort' : 80 })

    # Backend Service
    backendService = ecs.Ec2Service(self, "{}-backend".format(ns), task_definition=backendECSTask, cluster=self.bentoECS)

    # Frontend Service
    frontendService = ecs.Ec2Service(self, "{}-frontend".format(ns), task_definition=frontendECSTask, cluster=self.bentoECS)