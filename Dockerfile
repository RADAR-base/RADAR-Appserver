FROM openjdk:11.0.1-jdk-slim AS builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_OPTS -Dorg.gradle.daemon=false

COPY ./gradle /code/gradle
COPY ./gradlew /code/
RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /code/
COPY ./src /code/src

RUN ./gradlew unpack

FROM openjdk:11.0.1-jre-slim

MAINTAINER Yatharth Ranjan <yatharth.ranjan@kcl.ac.uk>

LABEL description="RADAR-base App server"

ENV JAVA_OPTS -Xmx2G -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod

VOLUME /tmp
ARG DEPENDENCY=/code/build/dependency

COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "app:app/lib/*", "org.radarbase.appserver.AppserverApplication"]