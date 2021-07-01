from aws_cdk import core
from aws_cdk import aws_ec2 as ec2

class EC2Resources:
  def createResources(self, ns):

    # Database EC2 Instance
    # AMI 
    neo4j_4 = linux = ec2.MachineImage.generic_linux({
        "us-east-1": "ami-0d5eaaf9327be9c1b"
        })

    # Instance
    self.DBInstance = ec2.Instance(self, 
        "Database Instance",
        instance_type=ec2.InstanceType(self.config[ns]['database_instance_type']),
        machine_image=neo4j_4,
        vpc = self.bentoVPC,
        role = self.ecsInstanceRole)
    core.Tags.of(self.DBInstance).add("Name", "{}-neo4j".format(ns))
    
    # Update DB Security Group
    dbsg = self.DBInstance.connections.security_groups[0]
    
    dbsg.add_ingress_rule(
        self.ecssg,
        ec2.Port.tcp(7474)
    )
    dbsg.add_ingress_rule(
        self.ecssg,
        ec2.Port.tcp(7687)
    )