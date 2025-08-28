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
                sh 'docker run -i --rm --name my-maven-check maven:3.9.9 mvn --version'
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Test') {
            steps {
                sh '''
                docker run -i --rm --name my-maven-build \
                  -v "${WORKSPACE}:/usr/src/mymaven" \
                  -w /usr/src/mymaven maven:3.9.9 \
                  mvn clean compile test
                '''
            }
        }
        
        stage('Package') {
            steps {
                sh '''
                docker run -i --rm --name my-maven-package \
                  -v "${WORKSPACE}:/usr/src/mymaven" \
                  -w /usr/src/mymaven maven:3.9.9 \
                  mvn package
                '''
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                sh '''
                docker run -i --rm --name my-maven-sonar \
                  -v "${WORKSPACE}:/usr/src/mymaven" \
                  -w /usr/src/mymaven maven:3.9.9 \
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
                        // Wait for webhook from SonarQube
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
            docker ps -aq --filter "name=my-maven-" | xargs -r docker rm -f || echo "No containers to clean"
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

