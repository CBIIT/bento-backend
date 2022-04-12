FROM jenkins/inbound-agent:latest-jdk8 as jnlp

FROM katalonstudio/katalon:8.2.5

COPY --from=jnlp /usr/local/bin/jenkins-agent /usr/local/bin/jenkins-agent
COPY --from=jnlp /usr/share/jenkins/agent.jar /usr/share/jenkins/agent.jar

RUN sed -i "s/https/http/g" /etc/apt/sources.list.d/google-chrome.list
RUN apt-get update && apt-get install -y apt-transport-https zip

ENTRYPOINT ["/usr/local/bin/jenkins-agent"]