pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://localhost:9000'
        // SonarQube token should be configured as Jenkins credential
        // SONAR_TOKEN will be injected from Jenkins credentials
        PROJECT_PATH = "${WORKSPACE}"
    }

    stages {
        stage('Maven Check') {
            steps {
                script {
                    echo 'Checking Maven version...'
                    sh 'docker run --rm --name maven-check maven:3.9.9 mvn --version'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo 'Building project with Maven...'
                    echo "Workspace: ${WORKSPACE}"
                    echo "Project Path: ${PROJECT_PATH}"
                    sh 'ls -la'  // Debug: show current directory contents
                    sh """
                        docker run --rm --name maven-build \\
                        -v "\$(pwd):/usr/src/mymaven" \\
                        -w /usr/src/mymaven \\
                        maven:3.9.9 \\
                        sh -c "ls -la && mvn clean install"
                    """
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    echo 'Running unit tests...'
                    sh """
                        docker run --rm --name maven-test \\
                        -v "\$(pwd):/usr/src/mymaven" \\
                        -w /usr/src/mymaven \\
                        maven:3.9.9 \\
                        mvn test
                    """
                }
            }
            post {
                always {
                    // Publish test results if they exist
                    script {
                        if (fileExists('target/surefire-reports/*.xml')) {
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    echo 'Running SonarQube analysis...'
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh """
                            docker run --rm --name maven-sonar \\
                            --network host \\
                            -v "\$(pwd):/usr/src/mymaven" \\
                            -w /usr/src/mymaven \\
                            maven:3.9.9 \\
                            mvn clean verify sonar:sonar \\
                            -Dsonar.projectKey=jenkins-test \\
                            -Dsonar.projectName='Jenkins Test Project' \\
                            -Dsonar.host.url=${SONAR_HOST_URL} \\
                            -Dsonar.token=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    echo 'Creating final package...'
                    sh """
                        docker run --rm --name maven-package \\
                        -v "\$(pwd):/usr/src/mymaven" \\
                        -w /usr/src/mymaven \\
                        maven:3.9.9 \\
                        mvn package
                    """
                }
            }
            post {
                success {
                    // Archive the built artifacts
                    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
            // Clean up workspace if needed
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check the logs for details.'
        }
    }
}
