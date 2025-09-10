pipeline {
    agent any // This uses the main Jenkins agent for all general tasks
    parameters {
        string(name: 'BRANCH', defaultValue: '', description: 'Branch to build (leave empty to use job-configured branch)')
        string(name: 'SONAR_PROJECT_KEY', defaultValue: '_6510110192', description: 'SonarQube project key')
        string(name: 'DEPLOY_PORT', defaultValue: '8001', description: 'Host port to bind the deployed container')
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
            steps {
                // create and run a test script inside python container to avoid host quoting issues
                writeFile file: 'run_tests.sh', text: '''#!/bin/bash
set -e
pip install --no-cache-dir -r requirements.txt
pytest --maxfail=1 --disable-warnings -q --cov=app --cov-report=xml --junitxml=results.xml
'''
                sh 'chmod +x run_tests.sh'
                sh 'echo "Running tests inside Docker container..."'
                sh 'docker run --rm -v "${WORKSPACE}":/usr/src -w /usr/src python:3.11 /usr/src/run_tests.sh'
                // archive junit/coverage artifacts
                junit allowEmptyResults: true, testResults: 'results.xml'
                archiveArtifacts artifacts: 'coverage.xml, results.xml', allowEmptyArchive: true
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
                                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                            -Dsonar.sources=. \
                                            -Dsonar.exclusions=**/*.java \
                                            -Dsonar.host.url=http://host.docker.internal:9000 \
                                            -Dsonar.login=${SCANNER_TOKEN}
                                        '''
                                }
                        }
                }

                stage('Quality Gate') {
                        steps {
                                // Poll SonarQube API for quality gate status. Fail the build if not OK.
                                withCredentials([string(credentialsId: 'sonarqube_token', variable: 'SCANNER_TOKEN')]) {
                                        sh '''
                                        echo "Checking SonarQube Quality Gate for project ${SONAR_PROJECT_KEY}..."
                                        attempts=12
                                        sleep_seconds=5
                                        status=UNKNOWN
                                        for i in $(seq 1 $attempts); do
                                            echo "Attempt $i/$attempts..."
                                            result=$(curl -s -u ${SCANNER_TOKEN}: "http://host.docker.internal:9000/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}")
                                            status=$(echo "$result" | grep -o '"status":"[^"]*' | head -1 | sed 's/"status":"//')
                                            echo "Raw status: $status"
                                            if [ "$status" != "IN_PROGRESS" ] && [ -n "$status" ]; then
                                                break
                                            fi
                                            sleep $sleep_seconds
                                        done
                                        echo "Final Quality Gate status: $status"
                                        if [ "$status" != "OK" ]; then
                                            echo "Quality Gate not passed. Output:";
                                            echo "$result";
                                            exit 1
                                        fi
                                        '''
                                }
                        }
                }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t fastapi-clean-demo:latest .'
            }
        }

        stage('Deploy Container (DinD)') {
            steps {
                // Use Docker-in-Docker: start a dind daemon in a user network and run docker client containers against it.
                sh '''
                echo "Starting DinD environment..."
                docker network create ci-net || true
                docker run --privileged -d --name dind --network ci-net -e DOCKER_TLS_CERTDIR= -p 2375:2375 docker:20-dind || true
                # wait for dockerd
                sleep 6
                echo "Building image using remote DinD daemon..."
                docker run --rm --network ci-net -v "${WORKSPACE}":/workspace -w /workspace -e DOCKER_HOST=tcp://dind:2375 docker:20 docker build -t fastapi-clean-demo:latest .
                echo "Stopping existing app and starting new one via DinD..."
                docker run --rm --network ci-net -e DOCKER_HOST=tcp://dind:2375 docker:20 docker stop fastapi_app || true
                docker run --rm --network ci-net -e DOCKER_HOST=tcp://dind:2375 docker:20 docker rm fastapi_app || true
                docker run -d --network ci-net -e DOCKER_HOST=tcp://dind:2375 -p ${DEPLOY_PORT}:8000 --name fastapi_app fastapi-clean-demo:latest
                # cleanup dind
                docker stop dind || true
                docker rm dind || true
                docker network rm ci-net || true
                '''
            }
        }
    }
}
