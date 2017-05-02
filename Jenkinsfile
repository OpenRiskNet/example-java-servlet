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
        sh 'docker push'
      }
    }  
  }
}
