FROM maven:3.8.6-jdk-8-slim as BUILD
WORKDIR /security-micro-service-build
COPY src .
RUN mvn versions:set -DnewVersion=20220427
RUN mvn clean install -DskipTests=true

FROM piandarduinoguy/python3-opencv:4.5.2
RUN apt update
RUN apt install -y openjdk-8-jre
WORKDIR /security-micro-service/resources
RUN echo "{\"securityStatus\":\"SAFE\",\"securityState\":\"DISARMED\"}" > security_config.json # initialize the security config file
WORKDIR /security-micro-service
COPY --from=BUILD /security-micro-service-build/target/security-micro-service-20220427.jar .
COPY --from=BUILD /security-micro-service-build/target/classes/yolo.tar.gz .
RUN tar xvzf yolo.tar.gz
CMD ["java", "-jar", "./security-micro-service-20220427.jar"]


