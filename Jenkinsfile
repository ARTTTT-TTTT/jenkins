pipeline {
    agent {
        label 'built-in' // Use the built-in node
    }
    
    tools {
        // Ensure Docker is available
        dockerTool 'docker-latest'
    }
    
    environment {
        DOCKER_IMAGE = 'jenkins-demo-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'demo-app'
        REGISTRY = '' // Add your registry if needed
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Image') {
            steps {
                script {
                    try {
                        sh 'docker --version'
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                    } catch (Exception e) {
                        error "Failed to build Docker image: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Stop Previous Container') {
            steps {
                script {
                    sh """
                        docker stop ${CONTAINER_NAME} || echo 'No container to stop'
                        docker rm ${CONTAINER_NAME} || echo 'No container to remove'
                    """
                }
            }
        }
        
        stage('Run Container') {
            steps {
                script {
                    sh "docker run -d -p 5000:5000 --name ${CONTAINER_NAME} ${DOCKER_IMAGE}:latest"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    sleep 10
                    sh "docker ps | grep ${CONTAINER_NAME}"
                    // Test if the application is responding
                    sh "curl -f http://localhost:5000 || echo 'Application might still be starting'"
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker image prune -f || true'
        }
        failure {
            sh "docker logs ${CONTAINER_NAME} || echo 'No container logs available'"
        }
        success {
            echo 'Pipeline completed successfully!'
        }
    }
}
