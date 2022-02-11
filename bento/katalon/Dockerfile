FROM jenkins/inbound-agent:latest-jdk8 as jnlp

FROM katalonstudio/katalon:8.0.5

COPY --from=jnlp /usr/local/bin/jenkins-agent /usr/local/bin/jenkins-agent
COPY --from=jnlp /usr/share/jenkins/agent.jar /usr/share/jenkins/agent.jar

ENTRYPOINT [“/usr/local/bin/jenkins-agent”]