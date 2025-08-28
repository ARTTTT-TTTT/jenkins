FROM jenkins/jenkins:latest 
  
USER root  
  
# Install dependencies  
RUN apt-get update && apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release  
  
# Add Docker GPG key  
RUN mkdir -p /etc/apt/keyrings && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg  
  
# Setup repository  
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null  
  
# Install Docker  
RUN apt-get update && apt-get install -y docker-ce docker-ce-cli containerd.io  
  
RUN usermod -aG docker jenkins  
USER jenkins 
