FROM maven
RUN mvn -B -f /webservice-springboot/pom.xml -s /usr/share/maven/ref/settings-docker.xml clean site
