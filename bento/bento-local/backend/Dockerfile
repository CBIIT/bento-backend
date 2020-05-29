# Pull the base image from Alpine
FROM alpine:latest AS base
MAINTAINER ESI Devops Team

ENV TOMCAT_MAJOR=9
ENV TOMCAT_VERSION=9.0.33
ENV CATALINA_HOME=/usr/local/tomcat
ENV TOMCAT_URL=https://archive.apache.org/dist/tomcat/tomcat-"$TOMCAT_MAJOR"/v"$TOMCAT_VERSION"/bin/apache-tomcat-"$TOMCAT_VERSION".tar.gz
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk
ENV NEW_RELIC_LOG=/usr/local/tomcat/newrelic/logs
ENV TOMCAT_USER=tomcat
ENV TOMCAT_GROUP=tomcat
ENV PATH=$CATALINA_HOME/bin:$JAVA_HOME/bin:$PATH
ENV JAVA_OPTS $JAVA_OPTS -javaagent:/usr/local/tomcat/newrelic/newrelic.jar -XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70

WORKDIR $CATALINA_HOME

RUN apk upgrade --update \
 && apk --no-cache add openjdk11-jdk --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN mkdir -p "$CATALINA_HOME" \
 && mkdir -p ${NEW_RELIC_LOG} \
 && wget -P newrelic "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar" \
 && addgroup -g 3001 -S ${TOMCAT_GROUP} \
 && adduser --disabled-password --ingroup "${TOMCAT_USER}" --uid 3001 ${TOMCAT_USER} \
 && echo ${TOMCAT_URL} \
 && wget -O tomcat.tar.gz $TOMCAT_URL \
 && tar -xf tomcat.tar.gz --strip-components=1 -C $CATALINA_HOME \
 && rm -rf tomcat.tar.gz \
 && chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} ${CATALINA_HOME} \
 && chmod +x ${CATALINA_HOME}/bin/*.sh


##### DEV BUILD STEPS: COPY PREBUILT CODE INTO CONTAINER #####

FROM base AS dev
RUN echo "Building DEV Install.."

RUN rm -rf ${CATALINA_HOME}/webapps/ROOT/
COPY backend/source/*.war ${CATALINA_HOME}/webapps/ROOT.war

USER ${TOMCAT_USER}

EXPOSE 8080
CMD ["catalina.sh","run"]

##############################################################

##### DEMO BUILD STEPS: BUILD CODE FROM GITHUB ################

FROM base AS demo
RUN apk add --virtual .build-deps --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community \
  git

# Download and install Maven
ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && echo "Downlaoding maven" \
  && wget -O /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  \
  && echo "Unziping maven" \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  \
  && echo "Cleaning and setting links" \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Define environmental variables required by Maven, like Maven_Home directory and where the maven repo is located
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# clone bento backend repo and fix config file for local connection
ARG NEO4J_USER
ARG NEO4J_PASS

RUN git clone https://github.com/CBIIT/bento-backend.git /usr/local/bento-backend \
 && cd /usr/local/bento-backend \
 && mv src/main/resources/application_example.properties src/main/resources/application.properties \
 && sed -i "s|neo4j.graphql.endpoint =http://localhost:7474/graphql/|neo4j.graphql.endpoint=http://neo4j:7474/graphql/|" src/main/resources/application.properties \
 && sed -i "s/spring.data.neo4j.username=neo4j_username/spring.data.neo4j.username=${NEO4J_USER}/" src/main/resources/application.properties \
 && sed -i "s/spring.data.neo4j.password=localhost/spring.data.neo4j.password=${NEO4J_PASS}/" src/main/resources/application.properties \
 && sed -i "s/neo4j.authorization =Basic 123456/neo4j.authorization =$(echo "Basic $(echo -n "${NEO4J_USER}:${NEO4J_PASS}" | base64)")/" src/main/resources/application.properties \
 && mvn package -DskipTests

# Copy war to tomcat webapps dir
RUN rm -rf ${CATALINA_HOME}/webapps/ROOT/ \
 && cp /usr/local/bento-backend/target/*.war ${CATALINA_HOME}/webapps/ROOT.war

# Cleanup build
RUN apk del .build-deps \
 && rm -rf ${MAVEN_HOME} \
 && rm -rf /usr/local/bento-backend

USER ${TOMCAT_USER}

EXPOSE 8080
CMD ["catalina.sh","run"]

##############################################################
