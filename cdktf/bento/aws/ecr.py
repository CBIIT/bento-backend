from imports.aws import EcrRepository

class ECRActions:
  def createECR(self):
    EcrRepository(self, "hellorepo", name="hellorepo")

#  def createECRPolicy(self):
#    EcrRepositoryPolicy(self, "helloecrpolicy", policy="hellopolicy", name="hellorepo")