# About
This repository contains the code needed to run the java service to perform object detection on 
images passed to it by the [security-camera-micro-service](https://github.com/PiAndArduinoGuy/security_camera_microservice)
as part of the [Security with Machine Learning Spice Makes Everything Nice](https://medium.com/dvt-engineering/security-with-machine-learning-spice-makes-everything-nice-778c1c3011b5) project.

# System Requirements
* Docker and Docker compose for Raspberry Pi - [How To Install Docker and Docker-Compose On Raspberry Pi](https://dev.to/elalemanyo/how-to-install-docker-and-docker-compose-on-raspberry-pi-1mo) 
* Windows or Linux operating system
* Git  - [How to Install Git on Raspberry Pi](https://linuxize.com/post/how-to-install-git-on-raspberry-pi/)

# Pre-Requisites
A docker swarm manager node needs to exist before proceeding. You will also need to know the token to join the swarm as a worker. If you do not yet have a host designated as a manager, the steps below
need to be followed to create one:

1. On the host designated as the manager, assign a static I.P. address to the host
    * if you opted to use a host running Ubuntu to be the manager, please follow the guide [Ubuntu Static I.P. Address](https://linuxconfig.org/how-to-configure-static-ip-address-on-ubuntu-18-10-cosmic-cuttlefish-linux#:~:text=Ubuntu%20Desktop,-The%20simplest%20approach&text=Click%20on%20the%20top%20right,netmask%2C%20gateway%20and%20DNS%20settings.) to set it up.
    * For a Raspberry Pi host please see [How to Setup a Raspberry Pi Static IP Address](https://pimylifeup.com/raspberry-pi-static-ip-address/)
    * For a Windows 10 machine - [How to Assign a Static IP Address in Windows 7, 8, 10, XP, or Vista](https://www.howtogeek.com/howto/19249/how-to-assign-a-static-ip-address-in-xp-vista-or-windows-7/)
2. execute the command `docker swarm init --advertise-addrr <static-ip-address-from-step-1>`
3. You will be presented with the token to be used to join the swarm as workers - note this command to be run later.

# Usage
1. On the host that will run the services defined in the docker-compose file, join the docker swarm using the command noted in step 3 of the pre-requisite section
2. Assign a static I.P. address to this host
3. Clone this repository onto your host
4. Navigate to the directory containing the docker-compose file
5. Replace the `<static-ip-address-here>` placeholder of the SECURITY_MICRO_SERVICE_BASE_URL environment variable with the value of the static I.P. address assigned in step 2 
6. Update any other environment variables as you please (but the defaults should suffice if you are following along with the [Security with Machine Learning Spice Makes Everything Nice](https://medium.com/dvt-engineering/security-with-machine-learning-spice-makes-everything-nice-778c1c3011b5) project)
7. Execute `docker-compose up -d` in the same directory as the `docker-compose.yml` file

# Help 
If you are stuck and need a hand, please reach out to me via email [piandarduinoguy@gmail.com](piandarduinoguy@gmail.com)