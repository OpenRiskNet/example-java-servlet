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
    stage('Docker image') {
      steps {
        sh './gradlew buildDockerfile'
      }
    }
  }
}