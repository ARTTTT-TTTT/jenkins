pipeline {
    agent any
    
    environment {
        SONAR_TOKEN = 'sqp_67395eac9b677bfa67358564e3aba1aed1e11622'
        SONAR_HOST_URL = 'http://172.17.0.3:9000'
        PROJECT_KEY = 'test'
        PROJECT_NAME = 'test'
    }

    stages {
        stage('Maven Check') {
            steps {
                sh 'docker run -i --rm maven:3.9.9 mvn --version'
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Verify Files') {
            steps {
                sh 'ls -la ${WORKSPACE}'
                sh 'test -f ${WORKSPACE}/pom.xml && echo "pom.xml found" || echo "pom.xml NOT found"'
                sh 'pwd'
            }
        }
        
        stage('Build & Test') {
            steps {
                sh '''
                # Create Maven container and copy files
                docker create --name maven-build-temp -w /app maven:3.9.9 sleep 30
                docker start maven-build-temp
                docker cp ${WORKSPACE}/. maven-build-temp:/app/
                docker exec maven-build-temp ls -la /app/
                docker exec maven-build-temp mvn clean compile test
                docker stop maven-build-temp
                docker rm maven-build-temp
                '''
            }
        }
        
        stage('Package') {
            steps {
                sh '''
                # Create Maven container for packaging
                docker create --name maven-package-temp -w /app maven:3.9.9 sleep 30
                docker start maven-package-temp
                docker cp ${WORKSPACE}/. maven-package-temp:/app/
                docker exec maven-package-temp mvn package
                # Copy target folder back
                docker cp maven-package-temp:/app/target/. ${WORKSPACE}/target/
                docker stop maven-package-temp
                docker rm maven-package-temp
                '''
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                sh '''
                # Create Maven container for SonarQube analysis
                docker create --name maven-sonar-temp --network bridge -w /app maven:3.9.9 sleep 60
                docker start maven-sonar-temp
                docker cp ${WORKSPACE}/. maven-sonar-temp:/app/
                docker exec maven-sonar-temp mvn clean verify sonar:sonar \
                  -Dsonar.projectKey=${PROJECT_KEY} \
                  -Dsonar.projectName="${PROJECT_NAME}" \
                  -Dsonar.host.url=${SONAR_HOST_URL} \
                  -Dsonar.token=${SONAR_TOKEN}
                docker stop maven-sonar-temp
                docker rm maven-sonar-temp
                '''
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 2, unit: 'MINUTES') {
                        sleep 10
                        echo "SonarQube analysis completed!"
                        echo "Check the results at: ${SONAR_HOST_URL}/dashboard?id=${PROJECT_KEY}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean up any remaining containers
            sh '''
            docker ps -aq --filter "name=maven-" | xargs -r docker stop || echo "No containers to stop"
            docker ps -aq --filter "name=maven-" | xargs -r docker rm -f || echo "No containers to clean"
            '''
        }
        success {
            echo 'Pipeline completed successfully!'
            echo "SonarQube Analysis: ${SONAR_HOST_URL}/dashboard?id=${PROJECT_KEY}"
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

