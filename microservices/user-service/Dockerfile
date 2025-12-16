FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/user-service/build.gradle.kts /code/microservices/user-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:user-service:downloadDependencies \
    microservices:user-service:copyDependencies \
    microservices:user-service:startScripts

COPY microservices/user-service/src/ /code/microservices/user-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/user-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/user-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/user-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/user-service/src/main/resources/user-service.yml /etc/user-service/user-service.yml

USER 101

EXPOSE 9013

ENTRYPOINT ["user-service"]
