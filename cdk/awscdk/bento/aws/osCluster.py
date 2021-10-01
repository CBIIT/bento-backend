from aws_cdk import aws_elasticsearch as es
from aws_cdk import core as cdk

class OSCluster:
  def createResources(self, ns):

    # ES Cluster
    self.esDomain = es.Domain(self, "bento-{}-elasticsearch".format(ns),
        version=es.ElasticsearchVersion.V7_1,
        domain_name="bento-{}-elasticsearch".format(ns),
        capacity={
            "data_node_instance_type": "t3.medium.elasticsearch",
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