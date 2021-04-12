from imports.aws import Alb

class ALBActions:
  def createALB(self):
    Alb(self, "hello-test-lb")