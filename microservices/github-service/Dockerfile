FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/github-service/build.gradle.kts /code/microservices/github-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:github-service:downloadDependencies \
    microservices:github-service:copyDependencies \
    microservices:github-service:startScripts

COPY microservices/github-service/src/ /code/microservices/github-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/github-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/github-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/github-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/github-service/src/main/resources/github-service.yml /etc/github-service/github-service.yml

USER 101

EXPOSE 9011

ENTRYPOINT ["github-service"]
