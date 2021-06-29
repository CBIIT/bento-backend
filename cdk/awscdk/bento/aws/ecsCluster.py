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
        key_name=self.config[ns]['ssh_key_name'],
        task_drain_time=cdk.Duration.minutes(0),
        min_capacity=int(self.config[ns]['min_size']),
        max_capacity=int(self.config[ns]['max_size']))