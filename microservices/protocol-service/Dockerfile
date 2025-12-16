FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/protocol-service/build.gradle.kts /code/microservices/protocol-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:protocol-service:downloadDependencies \
    microservices:protocol-service:copyDependencies \
    microservices:protocol-service:startScripts

COPY microservices/protocol-service/src/ /code/microservices/protocol-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/protocol-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/protocol-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/protocol-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/protocol-service/src/main/resources/protocol-service.yml /etc/protocol-service/protocol-service.yml

USER 101

EXPOSE 9012

ENTRYPOINT ["protocol-service"]
