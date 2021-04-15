# Bento cdktf project:  TEST

## Prerequisites

This project was built based on the python implementation detailed at:
- https://docs.aws.amazon.com/cdk/latest/guide/getting_started.html

The project can be built using the included docker-compose file to install prerequisites or they can be installed locally. 

### Using docker-compose

Once the repo has been cloned a dev container can be started from the cdktf folder using the following command:

```bash
docker-compose run aws-cdk sh
```

This will start a container with all required applications installed and map the awscdk/bento folder as its workspace.

## Initialize the bento cdk project

In order to build the bento cdktf files you will need to get the required python modules:

```bash
pip3 install --ignore-installed -r requirements.txt
```

And the cdktf modules and providers:

```bash
cdk get
```

## Build Cloudformation scripts for the bento cdk project

After modules are installed you can build terraform scripts from cdk:

```bash
cdk synth -a "python3 bento-aws.py -t <tier>"
```

* Note: an appropriate tier must be specified to build the bento scripts - if valid tiers are created or removed for this project getArgs.py must be updated to reflect these changes









python3 -m venv .venv
```

After the init process completes and the virtualenv is created, you can use the following
step to activate your virtualenv.

```
$ source .venv/bin/activate
```



Once the virtualenv is activated, you can install the required dependencies.

```
$ pip install -r requirements.txt
```