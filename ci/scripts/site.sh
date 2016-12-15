
set -e -x

mvn -f webservice-springboot -s /usr/share/maven/ref/settings-docker.xml clean site
cp -r webservice-springboot/target/* webservice-springboot-target