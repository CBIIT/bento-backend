from imports.aws import Instance

class EC2Actions:
  def createInstance(self, config, tier):
    Instance(self, "hello", ami="ami-2757f631", instance_type=config[tier]['fronted_instance_type'])