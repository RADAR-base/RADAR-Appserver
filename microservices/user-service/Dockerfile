FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/core/build.gradle.kts /code/microservices/core/
COPY microservices/contract/build.gradle.kts /code/microservices/contract/
COPY microservices/user-service/build.gradle.kts /code/microservices/user-service/

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    :microservices:user-service:downloadDependencies \
    :microservices:user-service:copyDependencies \
    :microservices:user-service:startScripts

COPY microservices/core/src/ /code/microservices/core/src/
COPY microservices/contract/src/ /code/microservices/contract/src/
COPY microservices/user-service/src/ /code/microservices/user-service/src/

RUN gradle --no-daemon :microservices:user-service:jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/user-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/user-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/user-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/user-service/src/main/resources/user-service.yml /etc/user-service/user-service.yml

ENV LIQUIBASE_ANALYTICS_ENABLED=false

USER 101

EXPOSE 9013

ENTRYPOINT ["user-service"]
