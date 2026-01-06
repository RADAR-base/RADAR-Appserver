FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/task-service/build.gradle.kts /code/microservices/task-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:task-service:downloadDependencies \
    microservices:task-service:copyDependencies \
    microservices:task-service:startScripts

COPY microservices/task-service/src/ /code/microservices/task-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/task-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/task-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/task-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/task-service/src/main/resources/task-service.yml /etc/task-service/task-service.yml

USER 101

EXPOSE 9014

ENTRYPOINT ["task-service"]
