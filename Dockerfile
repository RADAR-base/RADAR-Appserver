FROM --platform=$BUILDPLATFORM gradle:8.0-jdk17 AS builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
 GRADLE_OPTS='-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.daemon=false -Dorg.gradle.vfs.watch=false'

COPY ./build.gradle ./settings.gradle /code/

RUN gradle downloadDependencies copyDependencies

COPY ./src /code/src

RUN gradle unpack

FROM eclipse-temurin:17-jre

LABEL maintainer="Yatharth Ranjan <yatharth.ranjan@kcl.ac.uk>"

LABEL description="RADAR-base App server"

ENV JDK_JAVA_OPTIONS='-Xmx2G -Djava.security.egd=file:/dev/./urandom' \
    SPRING_PROFILES_ACTIVE=prod

VOLUME /tmp
ARG DEPENDENCY=/code/build/dependency

COPY --from=builder /code/build/third-party/* /app/lib/
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "app:app/lib/*", "org.radarbase.appserver.AppserverApplication"]
