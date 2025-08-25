pipeline {
  agent any
  parameters {
      booleanParam(name: 'RUN_DEPLOY', defaultValue: true, description: 'Should we deploy?')
      choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Select deployment environment')
  }
  stages {
      stage('Build') {
          steps {
              echo 'Building application...'
              // Simulate build process
              script {
                  env.BUILD_SUCCESS = 'true'
              }
          }
      }
      stage('Unit Tests') {
          when {
              expression { return env.BUILD_SUCCESS == 'true' }
          }
          steps {
              echo 'Running unit tests...'
              sh 'sleep 3'
              echo 'Unit tests completed successfully!'
          }
      }
      stage('OS Testing in Parallel') {
          parallel {
              stage('Linux Testing') {
                  steps {
                      echo '🐧 Running tests on Linux environment...'
                      sh 'echo "Linux: $(uname -s) tests passed!"'
                      sh 'sleep 2'
                  }
              }
              stage('Windows Testing') {
                  steps {
                      echo '🪟 Simulating tests on Windows environment...'
                      sh 'echo "Windows: Simulated Windows tests passed!"'
                      sh 'sleep 2'
                  }
              }
          }
      }
      stage('Integration Tests') {
          steps {
              echo 'Running integration tests...'
              sh 'sleep 3'
              sh 'echo "All integration tests passed!" > results.txt'
              archiveArtifacts artifacts: 'results.txt', fingerprint: true
          }
      }
      stage('Approval') {
          when {
              expression { return params.RUN_DEPLOY }
          }
          steps {
              timeout(time: 2, unit: 'MINUTES') {
                  input message: "Do you want to proceed with deployment to ${params.ENVIRONMENT}?", 
                        ok: 'Deploy',
                        submitterParameter: 'APPROVER'
              }
          }
      }
      stage('Deploy') {
          when {
              expression { return params.RUN_DEPLOY }
          }
          steps {
              echo "🚀 Deploying application to ${params.ENVIRONMENT} environment..."
              echo "Environment selected: ${params.ENVIRONMENT}"
              echo "Approved by: ${env.APPROVER}"
              sh 'sleep 3'
              echo "✅ Deployment to ${params.ENVIRONMENT} completed successfully!"
          }
      }
  }
   post {
    success {
        echo '✅ Pipeline finished successfully!'
    }
    failure {
        echo '❌ Pipeline failed. Check logs!'
    }
    always {
        echo 'Pipeline completed (success or failure).'
    }
  }
}
