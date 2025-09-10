pipeline {
    agent any // This uses the main Jenkins agent for all general tasks
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
                sh 'echo "Workspace cleaned"'
            }
        }

        stage('Checkout') {
            steps {
                // Use the pipeline's configured SCM (avoids hardcoding a branch that may not exist)
                checkout scm
                sh 'echo "=== FILES AFTER CHECKOUT ==="; ls -la'
            }
        }

        stage('Run Tests & Coverage') {
            steps {
                // Create a test runner script in the workspace and execute it inside the python container.
                // This avoids complex nested quoting and ensures the container sees the mounted files.
                sh '''
                echo "Running tests inside Docker container..."
                cat > run_tests.sh <<'SCRIPT'
                #!/bin/bash
                set -e
                pip install --no-cache-dir -r requirements.txt
                pytest --maxfail=1 --disable-warnings -q --cov=app --cov-report=xml
                SCRIPT
                chmod +x run_tests.sh
                docker run --rm -v "${PWD}":/usr/src -w /usr/src python:3.11 /usr/src/run_tests.sh
                echo "Tests completed."
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonarqube_token', variable: 'SCANNER_TOKEN')]) {
                        sh '''
                        echo "SUCCESS: SCANNER_TOKEN is set."
                        export SONAR_TOKEN="${SCANNER_TOKEN}"
                        /var/jenkins_home/workspace/FastAPI-Clean-Demo@2/sonar-scanner/bin/sonar-scanner -Dsonar.host.url="${SONAR_HOST_URL}"
                        '''
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                // Build using the host Docker CLI. The Jenkins agent must have access to Docker.
                sh 'docker build -t fastapi-clean-demo:latest .'
            }
        }

        stage('Deploy Container') {
            steps {
                // Deploy using host Docker CLI
                sh '''
                docker stop fastapi_app || true
                docker rm fastapi_app || true
                docker run -d -p 8001:8000 --name fastapi_app fastapi-clean-demo:latest
                '''
            }
        }
    }
}