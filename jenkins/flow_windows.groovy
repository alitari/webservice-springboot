def image = null
stage 'Package'
node {
  image = "${env.DOCKER_REGISTRY_URL}/alitari/webservice-springboot:0.1-${env.BUILD_NUMBER}"
  git url: 'https://github.com/alitari/webservice-springboot.git'
  bat "mvn clean package -DbuildNumber=${env.BUILD_NUMBER}"
  bat "copy target\\webservice-springboot-0.1-${env.BUILD_NUMBER}.jar target\\classes\\docker\\app.jar"
  bat "docker build -t ${image} target\\classes\\docker" 
}

stage 'Integration Tests'
node {
    def integrationTestPort = 81
    def container = "integration-test-webservice-springboot"
    try {
       bat "docker run --name ${container} -d -p ${integrationTestPort}:8080 ${image}" 
       def integrationTestHost = env.DOCKER_HOST.substring(6,env.DOCKER_HOST.lastIndexOf(":"))
       bat "mvn com.github.redfish4ktc.soapui:maven-soapui-extension-plugin:4.6.4.2:test -Dintegrationtest.host=${integrationTestHost} -Dintegrationtest.port=${integrationTestPort}"
       cleanUpContainer(container)
    } catch(e) { 
       cleanUpContainer(container)
       error "Error during running integration tests: ${e}"
    }
}

stage 'Persists artifacts'
node {
   bat "mvn deploy -DskipTests -DbuildNumber=${env.BUILD_NUMBER} -Did=releases"
   bat "docker push ${image}"
} 

def cleanUpContainer( String container) {
    bat "docker kill ${container}" 
    bat "docker rm ${container}" 
}