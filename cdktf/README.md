# Bento cdktf project:  TEST

## Prerequisites

This project was built based on the python-pip implementation detailed at:
- https://github.com/hashicorp/terraform-cdk/blob/main/docs/getting-started/python.md

The project can be built using the included docker-compose file to install prerequisites or they can be installed locally. 

### Using docker-compose

Once the repo has been cloned a dev container can be started using the following command:

```bash
docker-compose run cdktf sh
```

This will start a container with all required applications installed and map the cdktf bento folder as its workspace.

## Initialize the bento cdktf project

In order to build the bento cdktf files you will need to get the required python modules:

```bash
pip3 install --ignore-installed -r requirements.txt
```

And the cdktf modules and providers:

```bash
cdktf get
```

## Build Terraform scripts for the bento cdktf project

After modules are installed you can build terraform scripts from cdktf:

```bash
cdktf synth -a "python3 awsApp.py -t <tier>"
```

* Note that an appropriate tier must be specified to build the bento scripts - if valid tiers are created or removed for this project getArgs.py must be updated to reflect these changes