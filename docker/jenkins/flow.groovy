def dockerhost="192.168.0.16"
docker.image('maven:3.3.3-jdk-8').inside { 
  git url: 'https://github.com/alitari/webservice-springboot.git'
  writeFile file: 'settings.xml', text: "<settings><localRepository>${pwd()}/.m2repo</localRepository></settings>"
  sh "mvn -B -s settings.xml clean package -DbuildNumber=${env.BUILD_NUMBER}"
  sh "cp target/webservice-springboot-0.1-${env.BUILD_NUMBER}.jar ${pwd()}/docker/app/app.jar"
  sh " SONAR_USER_HOME=${pwd()}/.sonar mvn -B -s settings.xml sonar:sonar -Dsonar.host.url=http://${dockerhost}:9000 -Dsonar.jdbc.url=jdbc:postgresql://${dockerhost}:5432/sonar -DbuildNumber=${env.BUILD_NUMBER}"
}

node {
  def imageName = "${env.DOCKER_REGISTRY_URL}/alitari/webservice-springboot:0.1-${env.BUILD_NUMBER}"
  echo "Building image ${imageName}"
  def image =  docker.build "${imageName}", "${pwd()}/docker/app"
  image.withRun(' --name itest') { 
     docker.image('maven:3.3.3-jdk-8').inside("--link itest:itest") {   
        sh "mvn  -B -s settings.xml deploy -DskipTests -DbuildNumber=${env.BUILD_NUMBER} -Did=releases -Dintegrationtest.host=itest -Dintegrationtest.port=8080"
       
     }
  }  
 sh "docker push ${imageName}" 
}
