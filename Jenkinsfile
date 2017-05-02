pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './gradlew war'
      }
    }
    stage('Test') {
      steps {
        sh './gradlew test'
      }
    }
    stage('Docker build image') {
      steps {
        sh '''./gradlew buildDockerfile && \
  docker build -t informaticsmatters/orn-example-java-servlet build'''
      }
    }
    stage('Docker push image') {
      steps {
	sh 'docker login -u $DOCKER_HUB_TDUDGEON_USER -p $DOCKER_HUB_TDUDGEON_USER_PASSWORD'
        sh 'docker push informaticsmatters/orn-example-java-servlet'
      }
    }  
  }
}
