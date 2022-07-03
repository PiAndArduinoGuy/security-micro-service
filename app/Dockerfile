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

FROM amazoncorretto:8-alpine-jre AS run
COPY --from=builder /security-micro-service/target/security-micro-service-20220427.jar .
CMD ["java", "-jar", "./security-micro-service-20220427.jar"]

