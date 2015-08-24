docker.image('maven:3.3.3-jdk-8').inside { 
  git url: 'https://github.com/alitari/webservice-springboot.git'
  writeFile file: 'settings.xml', text: "<settings><localRepository>${pwd()}/.m2repo</localRepository></settings>"
  sh "mvn -B -s settings.xml clean package -DbuildNumber=${env.BUILD_NUMBER}"
  sh "cp target/webservice-springboot-0.1-${env.BUILD_NUMBER}.jar ${pwd()}/docker/app/app.jar"
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