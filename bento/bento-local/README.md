These files can be used to create a local bento environment using docker-compose. This will create all services and prepaqre the db to have data loaded.
Use the environment variables in the .env file to define which containers are for local development and which will have code built from github.

Docker commands:

install docker:

	curl -fsSL https://get.docker.com/ | sh

install docker-compose:

	curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
	chmod +x /usr/local/bin/docker-compose


docker-compose command to load bento infrastructure:

	COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose up -d

docker-compose command to rebuild an individual container:

	COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose up -d --no-deps --build <service_name>



clean all docker containers:

	docker system prune -a

clean all docker volumes:

	docker system prune --volumes

attach a shell to a running container:

	docker exec -it <container name> /bin/bash   (use /bin/ash for frontend and backend containers as they are based on alpine)


Notes on script behavior:

	- The script requires defining a .env file alongside the docker-compose.yml. This should be defined as:

	FRONT_ENV=  set this value to "dev" if you will be building the frontend container code locally, used for frontend development
	FRONTEND_BUILD_DIR=<value>  the directory you are building into for local frontend development
	BACKEND_IP=<value>  the IP address used to access the backend webapp
	BACK_ENV=  set this value to "dev" if you will be building the backend container code locally, used for backend development
	BACKEND_BUILD_DIR=<value>  the directory you are building into for local backend development
	NEO4J_USER=<value>  the user to set for Neo4j. The username is assumed to be the default "neo4j" if this is not set
	NEO4J_PASS=<value>  the password to set for Neo4j. The username is assumed to be the default "neo4j" if this is not set
