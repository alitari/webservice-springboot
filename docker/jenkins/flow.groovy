docker.image('maven:latest').inside('--net="host"') { 
  git url: 'https://github.com/alitari/webservice-springboot.git'
  writeFile file: 'settings.xml', text: "<settings><localRepository>${pwd()}/.m2repo</localRepository></settings>"
  sh "mvn -B -s settings.xml clean package -DbuildNumber=${env.BUILD_NUMBER}"
  sh "cp target/webservice-springboot-0.1-${env.BUILD_NUMBER}.jar ${pwd()}/docker/app/app.jar"
  sh " SONAR_USER_HOME=${pwd()}/.sonar mvn -B -s settings.xml sonar:sonar -Dsonar.host.url=http://docker_sonarqube_1:9000 -Dsonar.jdbc.url=jdbc:postgresql://docker_sonarqube_1:5432/sonar -DbuildNumber=${env.BUILD_NUMBER}"
}

node {
  def imageName = "registry:5000/alitari/webservice-springboot:0.1-${env.BUILD_NUMBER}"
  echo "Building image ${imageName}"
  def image =  docker.build "${imageName}", "${pwd()}/docker/app"
  image.withRun(' --name itest') { 
     docker.image('maven:latest').inside('--link="itest:itest"') {   
        sh "mvn  -B -s settings.xml com.github.redfish4ktc.soapui:maven-soapui-extension-plugin:4.6.4.2:test  -DbuildNumber=${env.BUILD_NUMBER} -Dintegrationtest.host=itest -Dintegrationtest.port=8080"  
     }
  }  
  docker.image('maven:latest').inside('--net="host"') {   
        sh "mvn  -B -s settings.xml deploy -DskipTests -DbuildNumber=${env.BUILD_NUMBER} -Did=releases"  
     }
 sh "docker push ${imageName}" 
}