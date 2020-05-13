Bento Dataloader Docker Implementation:

The Bento dataloader will pull required scripts and load locally stored data into Neo4j for a standalone Bento environment.

This project requires a set of folders containing data and configuration files:

     database
	     |
		 |___ config (holds all configuration files required to load data into Bento. The file names are defined in .env)
		 |
		 |___ data (holds data files to load into Neo4j)
		 

These scripts are designed to run using docker-compose using the command:  docker-compose build

Notes on script behavior:

	- The scripts will purge any previous data from the database and replace it with the data given to load
	- The scripts will not backup previously loaded data, any data in the database before the data loader is run will be lost
	- Currently the scripts skip validations, data will be loaded as it is defined in the data folder without testing
	- The script requires defining a .env file alongside the docker-compose.yml and Dockerfile. This should be defined as:
	
	NEO4J_PASS=<value>  the password set for the neo4j instance, the username is assumed to be the default "neo4j"
	NEO4J_IP=<value>  the IP address or DNS hostname of the neo4j server
	CONFIG_INI=<value>  the filename of the config ini file
	MODEL_GRAPHQL=<value>  the filename of the .graphql file
	MODEL_YML=<value>  the filename of the model .yml file
	PROPS_YML=<value>  the filename of the props .yml file
	MODEL_PROPS_YML=<value>  the filename of the model props .yml file
	

Docker notes:

install docker:

	curl -fsSL https://get.docker.com/ | sh

install docker-compose:

	curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
	chmod +x /usr/local/bin/docker-compose