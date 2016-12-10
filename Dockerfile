FROM maven
COPY pom.xml /tmp/pom.xml
RUN mvn -X -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
