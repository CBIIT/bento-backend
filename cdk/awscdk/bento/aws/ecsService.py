import boto3
from aws_cdk import aws_ecs as ecs
from aws_cdk import aws_ec2 as ec2

class ECSService:
  def createResources(self, ns):

    # Security Group Updates
    albsg = self.bentoALB.connections.security_groups[0]
    self.ecssg = self.bentoECS_ASG.connections.security_groups[0]
    
    botoec2 = boto3.client('ec2')
    group_name = 'bento-bastion-sg'
    response = botoec2.describe_security_groups(
        Filters=[
            dict(Name='group-name', Values=[group_name])
        ]
    )
    bastion_group_id = response['SecurityGroups'][0]['GroupId']
    self.bastionsg = ec2.SecurityGroup.from_security_group_id(self, 'bastion-security-group',
        security_group_id=bastion_group_id)

    self.ecssg.add_ingress_rule(
        albsg,
        ec2.Port.tcp(int(self.config[ns]['backend_container_port']))
    )
    self.ecssg.add_ingress_rule(
        albsg,
        ec2.Port.tcp(int(self.config[ns]['frontend_container_port']))
    )
    self.ecssg.add_ingress_rule(
        self.bastionsg,
        ec2.Port.tcp(22)
    )

    # Backend Task Definition
    backendECSTask = ecs.Ec2TaskDefinition(self, "bento-ecs-backend",
        network_mode=ecs.NetworkMode.AWS_VPC)
    
    backendECSContainer = backendECSTask.add_container('api',
        image = ecs.ContainerImage.from_registry("cbiitssrepo/bento-backend:latest"),
        memory_reservation_mib=1024,
        cpu=512)
    
    backend_port_mapping = ecs.PortMapping(
        container_port=int(self.config[ns]['backend_container_port']),
        host_port=int(self.config[ns]['backend_container_port']),
        protocol=ecs.Protocol.TCP
    )
    
    backendECSContainer.add_port_mappings(backend_port_mapping)

    # Backend Service
    self.backendService = ecs.Ec2Service(self, "{}-backend".format(ns),
        service_name="{}-backend".format(ns),
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
        container_port=int(self.config[ns]['frontend_container_port']),
        host_port=int(self.config[ns]['frontend_container_port']),
        protocol=ecs.Protocol.TCP
    )
    
    frontendECSContainer.add_port_mappings(frontend_port_mapping)

    # Frontend Service
    self.frontendService = ecs.Ec2Service(self, "{}-frontend".format(ns),
        service_name="{}-frontend".format(ns),
        task_definition=frontendECSTask,
        cluster=self.bentoECS)