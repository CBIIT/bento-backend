FROM alpine:latest
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
 
USER ${TOMCAT_USER} 

EXPOSE 8080
CMD ["catalina.sh","run"]