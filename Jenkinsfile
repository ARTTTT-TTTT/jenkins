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
            }
        }
        
        stage('Build & Test') {
            steps {
                sh '''
                docker run -i --rm \
                  --network bridge \
                  -v jenkins_home:/var/jenkins_home \
                  -w /var/jenkins_home/workspace/pipeline \
                  maven:3.9.9 \
                  mvn clean compile test
                '''
            }
        }
        
        stage('Package') {
            steps {
                sh '''
                docker run -i --rm \
                  --network bridge \
                  -v jenkins_home:/var/jenkins_home \
                  -w /var/jenkins_home/workspace/pipeline \
                  maven:3.9.9 \
                  mvn package
                '''
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                sh '''
                docker run -i --rm \
                  --network bridge \
                  -v jenkins_home:/var/jenkins_home \
                  -w /var/jenkins_home/workspace/pipeline \
                  maven:3.9.9 \
                  mvn clean verify sonar:sonar \
                  -Dsonar.projectKey=${PROJECT_KEY} \
                  -Dsonar.projectName="${PROJECT_NAME}" \
                  -Dsonar.host.url=${SONAR_HOST_URL} \
                  -Dsonar.token=${SONAR_TOKEN}
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
            sh 'docker ps -aq --filter "ancestor=maven:3.9.9" | xargs -r docker rm -f || echo "No containers to clean"'
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

