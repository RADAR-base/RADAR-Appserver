FROM openjdk:11.0.1-jdk-slim AS builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_OPTS -Dorg.gradle.daemon=false

COPY ./gradle /code/gradle
COPY ./gradlew /code/
RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /code/
COPY ./src /code/src
COPY ./shadow-radar-auth /code/shadow-radar-auth

RUN ./gradlew unpack

FROM openjdk:11.0.1-jre-slim

LABEL maintainer="Yatharth Ranjan <yatharth.ranjan@kcl.ac.uk>"

LABEL description="RADAR-base App server"

ENV JDK_JAVA_OPTIONS '-Xmx2G -Djava.security.egd=file:/dev/./urandom'
ENV SPRING_PROFILES_ACTIVE prod

VOLUME /tmp
ARG DEPENDENCY=/code/build/dependency

RUN apt-get update && apt-get install -y \
        curl \
        wget \
        && rm -rf /var/lib/apt/lists/*

COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "app:app/lib/*", "org.radarbase.appserver.AppserverApplication"]