from aws_cdk import aws_ecs as ecs
from aws_cdk import aws_ec2 as ec2
from aws_cdk import core as cdk

class ECSCluster:
  def createResources(self, ns):

    # ECS Cluster
    self.bentoECS = ecs.Cluster(self, "bento-ecs",
        cluster_name="{}".format(ns),
        vpc=self.bentoVPC)
    
    self.bentoECS_ASG = self.bentoECS.add_capacity('bento-ecs-instance',
        instance_type=ec2.InstanceType(self.config[ns]['fronted_instance_type']),
        task_drain_time=cdk.Duration.minutes(0),
        min_capacity=1,
        max_capacity=1)