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
                // Use the pipeline's configured SCM. This avoids hardcoding a branch (e.g. 'feature')
                // which may not exist on the remote and causes checkout failures.
                checkout scm
                sh 'echo "=== FILES AFTER CHECKOUT ==="; ls -la'
            }
        }

        stage('Run Tests & Coverage') {
            steps {
                // Run tests inside an ephemeral python container using the host Docker daemon.
                // This avoids using the declarative 'docker' agent which may not be available
                // on this Jenkins instance (plugin not installed).
                sh '''
                echo "Running tests inside Docker container..."
                docker run --rm -v "${PWD}":/usr/src -w /usr/src python:3.11 bash -lc "python -m venv venv && . venv/bin/activate && pip install --no-cache-dir -r requirements.txt && pytest --maxfail=1 --disable-warnings -q --cov=app --cov-report=xml"
                echo "Tests completed (exit status $? if any)."
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
                // Build using the host Docker CLI. Jenkins agent must have Docker access (socket)
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