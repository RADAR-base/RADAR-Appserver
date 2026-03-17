FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/core/build.gradle.kts /code/microservices/core/
COPY microservices/contract/build.gradle.kts /code/microservices/contract/
COPY microservices/cloud-messaging-service/build.gradle.kts /code/microservices/cloud-messaging-service/

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    :microservices:cloud-messaging-service:downloadDependencies \
    :microservices:cloud-messaging-service:copyDependencies \
    :microservices:cloud-messaging-service:startScripts

COPY microservices/core/src/ /code/microservices/core/src/
COPY microservices/contract/src/ /code/microservices/contract/src/
COPY microservices/cloud-messaging-service/src/ /code/microservices/cloud-messaging-service/src/

RUN gradle --no-daemon :microservices:cloud-messaging-service:jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/cloud-messaging-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/cloud-messaging-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/cloud-messaging-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/cloud-messaging-service/src/main/resources/cloud-messaging-service.yml /etc/cloud-messaging-service/cloud-messaging-service.yml

ENV LIQUIBASE_ANALYTICS_ENABLED=false

USER 101

EXPOSE 9015

ENTRYPOINT ["cloud-messaging-service"]
