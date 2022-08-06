FROM piandarduinoguy/python3-opencv
RUN apt install -y openjdk-8-jre
WORKDIR /security-micro-service/resources
RUN echo "{\"securityStatus\":\"SAFE\",\"securityState\":\"DISARMED\"}" > security_config.json # initialize the security config file
WORKDIR /security-micro-service
COPY ./target/security-micro-service-20220427.jar .
COPY ./src/main/resources/yolo.tar.gz .
RUN tar xvzf yolo.tar.gz
CMD ["java", "-jar", "./security-micro-service-20220427.jar"]


