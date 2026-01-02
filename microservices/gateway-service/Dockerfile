FROM --platform=$BUILDPLATFORM gradle:8.5-jdk17 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_USER_HOME=/code/.gradlecache \
    GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork -Dorg.gradle.vfs.watch=false"

COPY buildSrc /code/buildSrc
COPY build.gradle.kts settings.gradle.kts /code/
COPY microservices/gateway-service/build.gradle.kts /code/microservices/gateway-service/
COPY microservices/core/ /code/microservices/core/
COPY microservices/contract /code/microservices/contract

RUN gradle --no-daemon --no-parallel -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs="-Xmx1g" \
    microservices:gateway-service:downloadDependencies \
    microservices:gateway-service:copyDependencies \
    microservices:gateway-service:startScripts

COPY microservices/gateway-service/src/ /code/microservices/gateway-service/src/

RUN gradle jar

FROM eclipse-temurin:17-jre

COPY --from=builder /code/microservices/gateway-service/build/scripts/* /usr/bin/
COPY --from=builder /code/microservices/gateway-service/build/third-party/* /usr/lib/
COPY --from=builder /code/microservices/gateway-service/build/libs/*.jar /usr/lib/
COPY --from=builder /code/microservices/gateway-service/src/main/resources/gateway-service.yml /etc/gateway-service/gateway-service.yml

USER 101

EXPOSE 8080

CMD ["gateway-service"]
