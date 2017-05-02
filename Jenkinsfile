pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './gradlew clean war'
        archiveArtifacts artifacts: 'build/libs/*.war', fingerprint: false
      }
    }
    stage('Test') {
      steps {
        sh './gradlew test'
      }
      post {
        always {
          junit 'build/test-results/**/*.xml'
          archiveArtifacts artifacts: 'build/reports/**/*.*'
        }
      }
    }
    stage('Docker build image') {
      steps {
        sh '''./gradlew buildDockerfile && \
  docker build -t informaticsmatters/orn-example-java-servlet build'''
      }
    }
    stage('Docker push image') {
      environment { 
        DOCKER_CREDENTIALS = credentials('DOCKER_HUB_TDUDGEON') 
      }
      steps {
	sh 'docker login -u $DOCKER_CREDENTIALS_USR -p $DOCKER_CREDENTIALS_PSW'
        sh 'docker push informaticsmatters/orn-example-java-servlet'
      }
    }  
  }
}
