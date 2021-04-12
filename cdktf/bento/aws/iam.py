import json

from imports.aws import IamRole

class IAMActions:
  def createRole(self):
    policyDoc = json.dumps(dict([["Action", "sts:AssumeRole"], ["Effect", "Allow"], ["Principal", dict([['Service', 'ec2.amazonaws.com']])]]))

    IamRole(self, "helloIamRole", name="iamroleforhello", assume_role_policy=policyDoc)