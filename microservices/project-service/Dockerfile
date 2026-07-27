FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/core/build.gradle.kts /code/microservices/core/
COPY microservices/contract/build.gradle.kts /code/microservices/contract/
COPY microservices/project-service/build.gradle.kts /code/microservices/project-service/

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    :microservices:project-service:downloadDependencies \
    :microservices:project-service:copyDependencies \
    :microservices:project-service:startScripts

COPY microservices/core/src/ /code/microservices/core/src/
COPY microservices/contract/src/ /code/microservices/contract/src/
COPY microservices/project-service/src/ /code/microservices/project-service/src/

RUN gradle --no-daemon :microservices:project-service:jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/project-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/project-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/project-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/project-service/src/main/resources/project-service.yml /etc/project-service/project-service.yml

ENV LIQUIBASE_ANALYTICS_ENABLED=false

USER 101

EXPOSE 9010

ENTRYPOINT ["project-service"]
