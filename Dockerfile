FROM maven
COPY pom.xml /tmp/pom.xml
ADD src /tmp
RUN mvn -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml clean site