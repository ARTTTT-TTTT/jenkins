FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update && apt-get upgrade -y && curl -fsSL https://get.docker.com | sh
RUN usermod -aG docker jenkins

USER jenkins
