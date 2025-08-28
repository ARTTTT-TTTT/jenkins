# Jenkins Job Setup Instructions

## สร้าง Pipeline Job ใหม่:

### 1. สร้าง New Pipeline Job:

```
Jenkins Dashboard → New Item → Pipeline → ตั้งชื่อ "jenkins-maven-pipeline"
```

### 2. Configure Pipeline:

```
Pipeline Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/ARTTTT-TTTT/jenkins.git
Branch Specifier: */main
Script Path: Jenkinsfile
```

### 3. Credentials (ถ้าเป็น Private Repo):

```
Jenkins → Manage Jenkins → Credentials → Add Credentials
Username: GitHub username
Password: Personal Access Token
ID: github-credentials
```

### 4. SonarQube Token:

```
Jenkins → Manage Jenkins → Credentials → Add Credentials
Kind: Secret text
ID: sonar-token
Secret: sqp_67395eac9b677bfa67358564e3aba1aed1e11622
```

## Alternative: Multibranch Pipeline (แนะนำ)

### 1. สร้าง Multibranch Pipeline:

```
Jenkins Dashboard → New Item → Multibranch Pipeline
```

### 2. Configure Branch Source:

```
Branch Sources → Add source → Git
Project Repository: https://github.com/ARTTTT-TTTT/jenkins.git
Credentials: github-credentials (ถ้ามี)
```

### 3. Build Configuration:

```
Build Configuration → by Jenkinsfile
Script Path: Jenkinsfile
```

## Verification Steps:

1. ตรวจสอบ Webhook ใน GitHub (ถ้าต้องการ auto-trigger)
2. Run pipeline และดู console output
3. ตรวจสอบว่า workspace มีไฟล์ source code
