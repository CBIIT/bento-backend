import json
import imports.aws as aws

class EC2Resources:
  def createResources(self, ns, config, bentoTags, bentoIAM):

    # EC2
    # EC2 Instance
    testInstance = aws.Instance(self, "bento-test", ami="ami-2757f631", instance_type=config[ns]['fronted_instance_type'], iam_instance_profile=self.ecsInstanceProfile.name, tags=bentoTags)