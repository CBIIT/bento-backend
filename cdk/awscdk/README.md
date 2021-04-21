# Bento cdk project:  Bento AWS

## Prerequisites

This project was built based on the python implementation detailed at:
- https://docs.aws.amazon.com/cdk/latest/guide/getting_started.html

The project can be built using the included docker-compose file to install prerequisites or they can be installed locally. 


### Using docker-compose

Once the repo has been cloned a dev container can be started from the cdk/awscdk folder using the following command:

```bash
docker-compose run aws-cdk sh
```

This will start a container with all required applications installed and map the awscdk/bento folder as its workspace.


## Initialize the bento cdk project

In order to build the bento cdk files you will need to get the required python modules:

```bash
pip3 install --ignore-installed -r requirements.txt
```


## Build Cloudformation scripts for the bento cdk project

After modules are installed you can build cloudformation scripts from cdk:

```bash
cdk synth -a "python3 app.py -t <tier>"
```

* Note: an appropriate tier must be specified to build the bento scripts - if valid tiers are created or removed for this project getArgs.py must be updated to reflect these changes