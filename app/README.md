# About
This repository contains the code needed to run the java service needed to perform object detection on 
images passed to it by the [security-camera-micro-service](https://github.com/PiAndArduinoGuy/security_camera_microservice)
as part of the [Security with Machine Learning Spice Makes Everything Nice](https://medium.com/dvt-engineering/security-with-machine-learning-spice-makes-everything-nice-778c1c3011b5) project.

# System Requirements
* Docker and Docker compose for Raspberry Pi - [How To Install Docker and Docker-Compose On Raspberry Pi](https://dev.to/elalemanyo/how-to-install-docker-and-docker-compose-on-raspberry-pi-1mo) 
* Raspbian 11 (bullseye) OS version - [Install Raspberry Pi OS Bullseye on Raspberry Pi (Illustrative Guide)](https://raspberrytips.com/install-raspbian-raspberry-pi/)
* Git  - [How to Install Git on Raspberry Pi](https://linuxize.com/post/how-to-install-git-on-raspberry-pi/)
# Usage
1. Clone this repository onto your Raspberry Pi using Git
2. Navigate to the directory containing the docker-compose file
3. Update environment variables as you please (but the defaults should suffice if you are following along with the [Security with Machine Learning Spice Makes Everything Nice](https://medium.com/dvt-engineering/security-with-machine-learning-spice-makes-everything-nice-778c1c3011b5) project)
4. Execute `docker-compose up` in the same directory as the `docker-compose.yml` file

# Help 
If you are stuck and need a hand, please reach out to me via email [piandarduinoguy@gmail.com](piandarduinoguy@gmail.com)