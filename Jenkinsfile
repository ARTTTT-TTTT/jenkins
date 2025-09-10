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
                sh '''
                docker stop jenkins || true
                docker rm jenkins || true
                docker run -d -p 8001:8000 --name jenkins fastapi-clean-demo:latest
                '''
            }
        }
    }
}
