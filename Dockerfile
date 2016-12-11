FROM maven
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml clean site
