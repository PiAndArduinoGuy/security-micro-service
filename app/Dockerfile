# stages defined so that we only carry over what is needed to run the container
FROM ubuntu:bionic AS constructor
WORKDIR /security-micro-service
# multiple runs to use caching to best ability
RUN apt update
RUN apt-get install -y openjdk-8-jdk # maven requires java JDK
RUN apt-get install -y maven # maven to build project

FROM constructor AS builder
COPY src/ /security-micro-service/
RUN mvn versions:set -DnewVersion=20220427
RUN mvn clean install -DskipTests=true

FROM python:3.8-alpine as run
WORKDIR /security-micro-service
COPY --from=builder /security-micro-service/target/security-micro-service-20220427.jar .
COPY --from=builder /security-micro-service/src/main/resources/yolo/yolo.py .
RUN echo "{\"securityStatus\":\"SAFE\",\"securityState\":\"DISARMED\"}" > security-config.json # initialize the security config file
RUN apk add --no-cache openjdk8-jre
RUN apk add --no-cache bash # needed to start interactive exec session with container that uses this image for debug purposes
CMD ["java", "-jar", "./security-micro-service-20220427.jar"]
