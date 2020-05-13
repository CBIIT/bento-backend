# Pull the base image from Ubuntu
FROM ubuntu:latest AS base

RUN echo "Loading dataloader code and dependencies"

RUN apt-get update \
 && apt-get install -y python3 python3-pip git curl

RUN echo "cloning dataloader code" \
 && git clone --recurse-submodules -j8 https://github.com/CBIIT/icdc-dataloader.git /usr/local/neo4j_dataloader \
 && cd /usr/local/neo4j_dataloader \
 && pip3 install -r requirements.txt

# Data loading phase
FROM base AS load_data

# break caching for lines below this. This allows reloading data on every build without re-running setup tasks
ADD https://www.timeanddate.com/worldclock/usa/baltimore /tmp/bustcache

ARG NEO4J_PASS
ARG NEO4J_IP
ARG CONFIG_INI
ARG MODEL_GRAPHQL
ARG MODEL_YML
ARG PROPS_YML
ARG MODEL_PROPS_YML

RUN echo "Cleaning DB and loading data into Neo4j.."

# Copy data and config files to the container
ADD ./database /tmp/neo4j_data

RUN echo "loading provided dataset - NOTE: this will overwrite any current data" \
 && cd /usr/local/neo4j_dataloader \
 && python3 loader.py --wipe-db --no-backup -y -c -p ${NEO4J_PASS} -s /tmp/neo4j_data/config/${MODEL_YML} -s /tmp/neo4j_data/config/${MODEL_PROPS_YML}  --config-file /tmp/neo4j_data/config/${CONFIG_INI} --prop-file /tmp/neo4j_data/config/${PROPS_YML} -i bolt://${NEO4J_IP}:7687/ /tmp/neo4j_data/data \
 && basicAuth=$(echo "Basic $(echo -n "neo4j:${NEO4J_PASS}" | base64)") \
 && curl -X POST http://${NEO4J_IP}:7474/graphql/idl/ -H 'Accept: application/json' -H "Authorization: $basicAuth" -d @/tmp/neo4j_data/config/${MODEL_GRAPHQL}
