# Build stage
ARG ECR_REPO
FROM maven:3.6.3-openjdk-11 as build
WORKDIR /usr/src/app
COPY . .
RUN mvn package -DskipTests

# Production stage
FROM ncidockerhub.nci.nih.gov/icdc/bento-icdc-backend:release
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /usr/src/app/target/Bento-0.0.1.war /usr/local/tomcat/webapps/ROOT.war
