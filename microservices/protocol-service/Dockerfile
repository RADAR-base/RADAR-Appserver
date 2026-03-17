FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/core/build.gradle.kts /code/microservices/core/
COPY microservices/contract/build.gradle.kts /code/microservices/contract/
COPY microservices/protocol-service/build.gradle.kts /code/microservices/protocol-service/

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    :microservices:protocol-service:downloadDependencies \
    :microservices:protocol-service:copyDependencies \
    :microservices:protocol-service:startScripts

COPY microservices/core/src/ /code/microservices/core/src/
COPY microservices/contract/src/ /code/microservices/contract/src/
COPY microservices/protocol-service/src/ /code/microservices/protocol-service/src/

RUN gradle --no-daemon :microservices:protocol-service:jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/protocol-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/protocol-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/protocol-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/protocol-service/src/main/resources/protocol-service.yml /etc/protocol-service/protocol-service.yml

ENV LIQUIBASE_ANALYTICS_ENABLED=false

USER 101

EXPOSE 9012

ENTRYPOINT ["protocol-service"]
