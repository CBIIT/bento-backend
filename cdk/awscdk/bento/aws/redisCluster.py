from aws_cdk import aws_elasticache as ec
from aws_cdk import aws_ec2 as ec2

class RedisCluster:
  def createResources(self, ns):

    vpcPrivateSubnets = self.bentoVPC.select_subnets(subnet_type=ec2.SubnetType.PRIVATE)
    subnetIds = []
    for subnet in vpcPrivateSubnets.subnets:
      subnetIds.append(subnet.subnet_id)

    redisSubnetGroup = ec.CfnSubnetGroup(self, "Redis-{}-ClusterPrivateSubnetGroup".format(ns),
        cache_subnet_group_name="{}-private".format(ns),
        subnet_ids=subnetIds,
        description="{} private subnets".format(ns)
        )

    # Redis Cluster
    self.ecCluster = ec.CfnReplicationGroup(self, "bento-{}-redis-cluster".format(ns),
        engine="redis",
        cache_node_type="cache.t3.medium",
        replicas_per_node_group=1,
        #num_node_groups=1,
        multi_az_enabled=False,
        automatic_failover_enabled=True,
        auto_minor_version_upgrade=True,
        replication_group_description="redis bento-{} cluster".format(ns),
        cache_subnet_group_name=redisSubnetGroup.cache_subnet_group_name
        )
    self.ecCluster.add_depends_on(redisSubnetGroup)