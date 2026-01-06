FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/project-service/build.gradle.kts /code/microservices/project-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:project-service:downloadDependencies \
    microservices:project-service:copyDependencies \
    microservices:project-service:startScripts

COPY microservices/project-service/src/ /code/microservices/project-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/project-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/project-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/project-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/project-service/src/main/resources/project-service.yml /etc/project-service/project-service.yml

USER 101

EXPOSE 9010

ENTRYPOINT ["project-service"]

#CMD ["project-service"]
