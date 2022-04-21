curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip
unzip newrelic-java.zip 
mkdir -p /usr/local/tomcat/newrelic
cp ./newrelic/newrelic.jar /usr/local/tomcat/newrelic/newrelic.jar
cp ./new-relic/newrelic.yml /usr/local/tomcat/newrelic/newrelic.yml
