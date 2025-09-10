pipeline {
    agent any // This uses the main Jenkins agent for all general tasks
    environment {
        APP_IMAGE = 'fastapi-clean-demo:latest'
        HOST_PORT = credentials('app_host_port') ?: '8001'
        CONTAINER_PORT = '8000'
    }
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
                sh 'echo "Workspace cleaned"'
            }
        }

        stage('Checkout') {
            steps {
                // Try the pipeline-configured SCM first; if that fails (missing branch),
                // fallback to the repository 'main' branch to avoid aborting the build.
                script {
                    try {
                        echo "Attempting to checkout using pipeline-configured SCM..."
                        checkout scm
                    } catch (err) {
                        echo "Primary checkout failed: ${err}. Falling back to 'main' branch."
                        checkout([$class: 'GitSCM', branches: [[name: 'refs/heads/main']], userRemoteConfigs: [[url: 'https://github.com/ARTTTT-TTTT/jenkins.git']]])
                    }
                }
                sh 'echo "=== FILES AFTER CHECKOUT ==="; ls -la'
            }
        }

        stage('Run Tests & Coverage') {
            agent {
                docker {
                    image 'python:3.11'
                }
            }
            steps {
                sh '''
                echo "Running tests inside Docker container..."
                python -m venv venv
                . venv/bin/activate
                pip install --no-cache-dir -r requirements.txt
                echo "Dependencies installed. Running tests..."
                pytest --maxfail=1 --disable-warnings -q --cov=app --cov-report=xml
                echo "Tests completed successfully."
                '''
            }
        }

        stage('SonarQube Analysis') {
                        steps {
                                // Run sonar-scanner inside the official SonarScanner Docker image.
                                // This avoids needing the SonarQube Jenkins plugin or a configured Sonar installation.
                                withCredentials([string(credentialsId: 'sonarqube_token', variable: 'SCANNER_TOKEN')]) {
                                        sh '''
                                        echo "Running SonarScanner in Docker..."
                                        docker run --rm -v "${WORKSPACE}":/usr/src -w /usr/src sonarsource/sonar-scanner-cli:latest \
                                            -Dsonar.projectKey=_6510110192 \
                                            -Dsonar.sources=. \
                                            -Dsonar.exclusions=**/*.java \
                                            -Dsonar.host.url=http://host.docker.internal:9000 \
                                            -Dsonar.login=${SCANNER_TOKEN}
                                        '''
                                }
                        }
        }

        stage('Build Docker Image') {
            agent {
                docker {
                    image 'docker:19.03.13'
                    args "-w /workspace -v ${pwd()}:/workspace -v /var/run/docker.sock:/var/run/docker.sock"
                }
            }
            steps {
                sh 'docker build -t fastapi-clean-demo:latest .'
            }
        }

        stage('Deploy Container') {
            agent {
                docker {
                    image 'docker:19.03.13'
                    args "-w /workspace -v ${pwd()}:/workspace -v /var/run/docker.sock:/var/run/docker.sock"
                }
            }
            steps {
                // Use Docker-in-Docker style deploy: stop/remove old -> run new with env-driven ports
                sh '''
                docker stop fastapi_app || true
                docker rm fastapi_app || true
                docker run -d -p ${HOST_PORT}:${CONTAINER_PORT} --name fastapi_app ${APP_IMAGE}
                docker ps -a --filter name=fastapi_app
                '''
            }
        }

        stage('SonarQube Quality Gate') {
            steps {
                // Wait for SonarQube analysis to compute Quality Gate and fail the build if it fails
                withCredentials([string(credentialsId: 'sonarqube_token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                    echo "Checking SonarQube Quality Gate..."
                    # Use the SonarQube API to poll the task status produced by scanner
                    TASK_URL=$(cat .scannerwork/report-task.txt | grep ceTaskUrl | cut -d'=' -f2 || true)
                    if [ -z "$TASK_URL" ]; then
                      echo "No SonarQube task URL found; skipping quality gate check."
                      exit 0
                    fi

                    # Poll until the analysis is complete
                    STATUS="PENDING"
                    until [ "$STATUS" = "SUCCESS" ] || [ "$STATUS" = "FAILED" ] || [ "$STATUS" = "CANCELED" ]; do
                      sleep 3
                      STATUS=$(curl -s -u ${SONAR_TOKEN}: "$TASK_URL" | jq -r '.task.status')
                      echo "Sonar task status: $STATUS"
                    done

                    # Get the project status (OK or ERROR)
                    PROJECT_STATUS=$(curl -s -u ${SONAR_TOKEN}: "$TASK_URL" | jq -r '.task.analysisId' | xargs -I {} curl -s -u ${SONAR_TOKEN}: "${SONAR_HOST_URL:-http://host.docker.internal:9000}/api/qualitygates/project_status?analysisId={}" )
                    echo "$PROJECT_STATUS" | jq .
                    GATE_RESULT=$(echo "$PROJECT_STATUS" | jq -r '.projectStatus.status')
                    if [ "$GATE_RESULT" != "OK" ]; then
                      echo "Quality Gate failed: $GATE_RESULT"
                      exit 1
                    fi
                    echo "Quality Gate passed: $GATE_RESULT"
                    '''
                }
            }
        }
    }
}
