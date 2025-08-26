pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Image') {
            steps {
                sh 'docker build -t jenkins-demo-app:latest .'
            }
        }
        stage('Run Container') {
            steps {
                sh '''
                    docker stop demo-app || true
                    docker rm demo-app || true
                    docker run -d -p 5000:5000 --name demo-app jenkins-demo-app:latest
                '''
            }
        }
    }
}
