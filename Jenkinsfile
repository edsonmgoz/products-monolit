pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-25'
        }
    }
    triggers {
        githubPush()
    }
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=${WORKSPACE}/.m2"
    }
    stages {
        stage('Compile') {
            steps {
                sh 'mvn clean compile -B -ntp'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test -B -ntp'
            }
            post {
                success {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Coverage') {
            steps {
                sh 'mvn jacoco:report -B -ntp'
            }
            post {
                success {
                    recordCoverage(tools: [[parser: 'JACOCO']])
                }
            }
        }
    }
    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        cleanup {
            cleanWs()
        }
    }
}
