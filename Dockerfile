FROM maven:3.6.3-jdk-8-slim AS build
COPY pom.xml /tmp
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean package

FROM openjdk:8u181

ENV VERSION "2.0"
ENV PROJECT_NAME "sherlock-anomaly-detection"

RUN mkdir -p /sherlock

WORKDIR /sherlock

COPY --from=build /tmp/target/*-jar-with-dependencies.jar /sherlock/sherlock.jar
RUN chmod +x sherlock.jar

ENTRYPOINT ["sh", "-c", \
        "java -jar sherlock.jar --version ${VERSION} --project-name ${PROJECT_NAME} $0 $@"]