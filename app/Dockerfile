# stages defined so that we only carry over what is needed to run the container
FROM python3-opencv:4.5.2
WORKDIR /security-micro-service
RUN apt install -y openjdk-8-jre
RUN echo "{\"securityStatus\":\"SAFE\",\"securityState\":\"DISARMED\"}" > security_config.json # initialize the security config file
COPY ./src/target/security-micro-service-20220427.jar .
COPY ./src/src/main/resources/yolo.tar.gz .
RUN tar xvzf yolo.tar.gz
CMD ["java","-agentlib:jdwp=transport=dt_socket,address=18082,server=y,suspend=y", "-jar", "./security-micro-service-20220427.jar"]
