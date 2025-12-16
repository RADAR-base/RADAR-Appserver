FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/cloud-messaging-service/build.gradle.kts /code/microservices/cloud-messaging-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:cloud-messaging-service:downloadDependencies \
    microservices:cloud-messaging-service:copyDependencies \
    microservices:cloud-messaging-service:startScripts

COPY microservices/cloud-messaging-service/src/ /code/microservices/cloud-messaging-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/cloud-messaging-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/cloud-messaging-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/cloud-messaging-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/cloud-messaging-service/src/main/resources/cloud-messaging-service.yml /etc/cloud-messaging-service/cloud-messaging-service.yml

USER 101

EXPOSE 9014

ENTRYPOINT ["cloud-messaging-service"]
