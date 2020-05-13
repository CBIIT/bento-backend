# Pull the base image from Neo4j
FROM neo4j:3.5.14-enterprise

# Download and install GraphQL
ARG GRAPHQL_VERSION=3.5.0.4
ARG NEO4J_PLUGIN_DIR="/var/lib/neo4j/plugins"
ARG GRAPHQL_URL=https://github.com/neo4j-graphql/neo4j-graphql/releases/download/${GRAPHQL_VERSION}

RUN apt-get update \
 && apt-get install -y wget

RUN echo "Downlaoding GraphQL ${GRAPHQL_VERSION}" \
 && wget -O ${NEO4J_PLUGIN_DIR}/neo4j-graphql-${GRAPHQL_VERSION}.jar ${GRAPHQL_URL}/neo4j-graphql-${GRAPHQL_VERSION}.jar \
 && echo "dbms.unmanaged_extension_classes=org.neo4j.graphql=/graphql" >> /var/lib/neo4j/conf/neo4j.conf \
 && echo "dbms.directories.plugins=plugins" >> /var/lib/neo4j/conf/neo4j.conf