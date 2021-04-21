#!/usr/bin/env python3
import os, sys

from configparser import ConfigParser
from getArgs import getArgs
from aws_cdk import core as cdk
from aws_cdk import core

from bento.bento_stack import BentoStack

if __name__=="__main__":
  tierName = getArgs.set_tier(sys.argv[1:])
  if not tierName:
    print('Please specify the tier to build:  awsApp.py -t <tier>')
    sys.exit(1)

  app = core.App()
  bentoApp = BentoStack(app, tierName)

  bentoTags = dict(s.split(':') for s in bentoApp.config['bento-cdk']['tags'].split(","))

  for tag,value in bentoTags.items():
    core.Tags.of(bentoApp).add(tag, value)

  app.synth()