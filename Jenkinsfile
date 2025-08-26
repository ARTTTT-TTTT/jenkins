pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/ARTTTT-TTTT/jenkins'
            }
        }
        stage('Build Image') {
            steps {
                sh 'docker build -t jenkins-demo-app:latest .'
            }
        }
        stage('Run Container') {
            steps {
                sh 'docker rm -f demo-app || true'
                sh 'docker run -d -p 8081:8081 --name demo-app jenkins-demo-app:latest'
            }
        }
    }
}
