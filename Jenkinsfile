pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://localhost:9000'
        // SonarQube token should be configured as Jenkins credential
        // SONAR_TOKEN will be injected from Jenkins credentials
        PROJECT_PATH = "${WORKSPACE}"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo 'Checking out source code...'
                    // Clean workspace first
                    deleteDir()
                    
                    // Try multiple checkout methods
                    try {
                        // Method 1: Standard SCM checkout
                        checkout scm
                        echo "✅ Standard SCM checkout successful"
                    } catch (Exception e) {
                        echo "❌ Standard SCM failed: ${e.getMessage()}"
                        
                        // Method 2: Manual Git checkout (if needed)
                        // Uncomment and modify if you have Git repository
                        /*
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: 'main']],
                            userRemoteConfigs: [[url: 'https://github.com/ARTTTT-TTTT/jenkins.git']]
                        ])
                        */
                    }
                    
                    // Debug: show all files
                    sh 'pwd && ls -la'
                    sh 'find . -name "pom.xml" -type f || echo "No pom.xml found"'
                }
            }
        }
        
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
                    sh 'pwd && ls -la'
                    
                    // Check if pom.xml exists
                    def pomExists = fileExists('pom.xml')
                    echo "POM exists: ${pomExists}"
                    
                    if (!pomExists) {
                        error "pom.xml not found in workspace! Please check SCM configuration."
                    }
                    
                    sh """
                        docker run --rm --name maven-build \\
                        -v "${WORKSPACE}:/usr/src/mymaven" \\
                        -w /usr/src/mymaven \\
                        maven:3.9.9 \\
                        sh -c "pwd && ls -la && cat pom.xml | head -10 && mvn clean install"
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
                        -v "${WORKSPACE}:/usr/src/mymaven" \\
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
                            -v "${WORKSPACE}:/usr/src/mymaven" \\
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
                        -v "${WORKSPACE}:/usr/src/mymaven" \\
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
