import os
from aws_cdk import core
from aws_cdk import aws_ec2 as ec2

class EC2Resources:
  def createResources(self, ns):

    # Database EC2 Instance
    # AMI 
    neo4j_4 = ec2.MachineImage.generic_linux({
        "us-east-1": os.environ.get('DATABASE_AMI_ID')
        })

    # User Data Script
    initFile = open("aws/scripts/db_init.sh")
    initScript = initFile.read()
    initFile.close()
    
    # Instance
    self.DBInstance = ec2.Instance(self, 
        "{}-Database-Instance".format(ns),
        instance_type=ec2.InstanceType(os.environ.get('DATABASE_INSTANCE_TYPE')),
        machine_image=neo4j_4,
        key_name=os.environ.get('SSH_KEY_NAME'),
        vpc=self.bentoVPC,
        role=self.ecsInstanceRole)
    self.DBInstance.add_user_data(initScript)
    core.Tags.of(self.DBInstance).add("Name", "{}-neo4j-4".format(ns))
    
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
    dbsg.add_ingress_rule(
        self.bastionsg,
        ec2.Port.tcp(22)
    )