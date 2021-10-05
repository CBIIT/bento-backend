from aws_cdk import aws_opensearchservice as os
from aws_cdk import aws_ec2 as ec2
from aws_cdk import core as cdk

class OSCluster:
  def createResources(self, ns):

    vpcPrivateSubnets = self.bentoVPC.select_subnets(subnet_type=ec2.SubnetType.PRIVATE)
    
    # OS Cluster
    self.osDomain = os.Domain(self, "bento-{}-elasticsearch".format(ns),
        version=os.EngineVersion.ELASTICSEARCH_7_10,
        domain_name="bento-{}-es".format(ns),
        vpc=self.bentoVPC,
        capacity={
            "data_node_instance_type": "t3.medium.search",
            "data_nodes": 2
        },
        ebs={
            "volume_size": 120
        },
        zone_awareness={
            "availability_zone_count": 2
        },
        logging={
            "slow_index_log_enabled": True
        }
    )