- `SonarQube`

```bash
docker run -it --rm --name my-maven-project -v "%cd%:/usr/src/mymaven" -w /usr/src/mymaven maven:3.9.9 mvn clean verify sonar:sonar -Dsonar.projectKey=test -Dsonar.projectName="test" -Dsonar.host.url=http://172.17.0.3:9000 -Dsonar.token=sqp_67395eac9b677bfa67358564e3aba1aed1e11622
```
