def image = null
stage 'Package'
node {
  image = "${env.DOCKER_REGISTRY_URL}/alitari/webservice-springboot:0.1-${env.BUILD_NUMBER}"
  git url: 'https://github.com/alitari/webservice-springboot.git'
  def mvnHome = tool 'maven-3.2.3'
  bat "mvn clean package -DbuildNumber=${env.BUILD_NUMBER}"
  bat "copy target\\webservice-springboot-0.1-${env.BUILD_NUMBER}.jar target\\classes\\docker\\app.jar"
  bat "docker build -t ${image} target\\classes\\docker" 
}

stage 'Integration Tests'
node {
    def container = "integration-test-webservice-springboot"
    bat "docker run --name ${container} -d -p 81:8080 ${image}" 
    bat "mvn com.github.redfish4ktc.soapui:maven-soapui-extension-plugin:4.6.4.2:test -Dintegrationtest.host=192.168.99.100 -Dintegrationtest.port=81"
    bat "docker kill ${container}" 
    bat "docker rm ${container}" 
    
}

stage 'Persists artifacts'
node {
   bat "mvn deploy -DskipTests -DbuildNumber=${env.BUILD_NUMBER} -Did=releases"
   bat "docker push ${image}"
} 