FROM --platform=$BUILDPLATFORM gradle:8.13-jdk17 AS builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
 GRADLE_OPTS='-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.daemon=false -Dorg.gradle.vfs.watch=false'

COPY ./build.gradle ./settings.gradle ./gradlew /code/
COPY ./gradle /code/gradle
COPY ./appserver-legacy/build.gradle /code/appserver-legacy/

RUN gradle :appserver-legacy:downloadDependencies :appserver-legacy:copyDependencies

COPY ./appserver-legacy/src /code/appserver-legacy/src

RUN gradle :appserver-legacy:unpack

FROM eclipse-temurin:17-jre

LABEL maintainer="Yatharth Ranjan <yatharth.ranjan@kcl.ac.uk>"

LABEL description="RADAR-base App server (legacy)"

ENV JDK_JAVA_OPTIONS='-Xmx2G -Djava.security.egd=file:/dev/./urandom' \
    SPRING_PROFILES_ACTIVE=prod \
    # Globally disables liquibase analytics.
    # (see: https://docs.liquibase.com/pro/user-guide/what-data-does-liquibase-collect-and-how-is-it-used)
    LIQUIBASE_ANALYTICS_ENABLED=false

VOLUME /tmp
ARG DEPENDENCY=/code/appserver-legacy/build/dependency

COPY --from=builder /code/appserver-legacy/build/third-party/* /app/lib/
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "app:app/lib/*", "org.radarbase.appserver.AppserverApplication"]
