# Jenkins CI/CD Pipeline Project

This project contains a Jenkins pipeline configuration for building Java applications with Maven, running tests, and performing SonarQube code analysis.

## 🛡️ Security Configuration

### SonarQube Token Setup

**Important**: Never commit SonarQube tokens to version control!

To configure the SonarQube token in Jenkins:

1. Go to Jenkins Dashboard → Manage Jenkins → Credentials
2. Click "Add Credentials"
3. Choose "Secret text"
4. Set ID: `sonar-token`
5. Set Secret: Your SonarQube token (`sqp_...`)
6. Description: "SonarQube Authentication Token"

### Environment Variables

The pipeline uses these environment variables:

- `SONAR_HOST_URL`: SonarQube server URL (default: http://localhost:9000)
- `SONAR_TOKEN`: Retrieved from Jenkins credentials (ID: `sonar-token`)
- `PROJECT_PATH`: Dynamically set to Jenkins workspace

## 🏗️ Project Structure

```
├── src/
│   ├── main/java/hello/
│   │   └── HelloWorld.java
│   └── test/java/hello/
│       └── HelloWorldTest.java
├── pom.xml
├── Dockerfile
├── Jenkinsfile
└── .gitignore
```

## 🚀 Pipeline Stages

1. **Maven Check**: Verify Maven installation
2. **Build**: Compile project with `mvn clean install`
3. **Test**: Run unit tests with JUnit reporting
4. **SonarQube Analysis**: Code quality analysis
5. **Package**: Create JAR artifacts

## 🐳 Docker Integration

All Maven operations run inside Docker containers to ensure consistency across environments.

## 📋 Prerequisites

- Jenkins with Docker support
- SonarQube server running on localhost:9000
- Docker engine accessible from Jenkins

## ⚠️ Security Notes

- SonarQube tokens are stored as Jenkins credentials
- No sensitive data is committed to repository
- Workspace cleanup after each build
