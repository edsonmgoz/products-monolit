pipeline {
    agent none
    triggers {
        githubPush()
    }
    environment {
        DOCKER_IMAGE = 'edsonmgoz/products-monolit'
    }
    stages {
        stage('CI') {
            agent {
                docker { image 'maven:3.9-eclipse-temurin-25' }
            }
            environment {
                MAVEN_OPTS      = "-Dmaven.repo.local=${WORKSPACE}/.m2"
                SONAR_USER_HOME = "${WORKSPACE}/.sonar"
            }
            stages {
                stage('Compile') {
                    steps {
                        script {
                            def pom = readMavenPom file: 'pom.xml'
                            env.IMAGE_TAG = pom.version
                        }
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
                stage('Package') {
                    steps {
                        sh 'mvn package -DskipTests -B -ntp'
                    }
                    post {
                        success {
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                            stash name: 'application-jar', includes: 'target/*.jar'
                        }
                    }
                }
                stage('SonarQube') {
                    steps {
                        withSonarQubeEnv('sonarqube') {
                            script {
                                if (env.CHANGE_ID) {
                                    sh """
                                        mvn sonar:sonar -B -ntp \
                                        -Dsonar.pullrequest.key=${env.CHANGE_ID} \
                                        -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH} \
                                        -Dsonar.pullrequest.base=${env.CHANGE_TARGET}
                                    """
                                } else {
                                    def branchName = GIT_BRANCH.replaceFirst('^origin/', '')
                                    sh "mvn sonar:sonar -B -ntp -Dsonar.branch.name=${branchName} -Dsonar.branch.target=${branchName}"
                                }
                            }
                        }
                    }
                }
                stage('Publish') {
                    steps {
                        script {
                            def server = Artifactory.server 'artifactory'
                            def pom = readMavenPom file: 'pom.xml'
                            def groupIdPath = pom.groupId.replaceAll("\\.", "/")
                            def repo = pom.version.endsWith('SNAPSHOT') ? 'products-monolit-snapshot' : 'products-monolit-release'
                            def uploadSpec = """
                                {
                                    "files": [
                                        {
                                            "pattern": "target/.*.jar",
                                            "target": "${repo}/${groupIdPath}/${pom.artifactId}/${pom.version}/",
                                            "regexp": "true",
                                            "props": "build.url=${RUN_DISPLAY_URL};build.user=${USER}"
                                        }
                                    ]
                                }
                            """
                            def buildInfo = server.upload spec: uploadSpec
                            server.publishBuildInfo buildInfo
                        }
                    }
                }
            }
            post {
                always {
                    cleanWs()
                }
            }
        }
        stage('Docker Build') {
            agent any
            steps {
                unstash 'application-jar'
                sh "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:latest ."
            }
        }
        stage('Docker Push') {
            agent any
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                        docker logout
                    """
                }
            }
        }
        stage('Deploy') {
            agent any
            steps {
                sh "TAG=${IMAGE_TAG} docker compose up -d --pull always --remove-orphans"
            }
        }
    }
}
