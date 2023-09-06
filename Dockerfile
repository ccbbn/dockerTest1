FROM gradle:7.4-jdk11-alpine as builder

WORKDIR /build


COPY build.gradle settings.gradle /build/
RUN gradle build -x test --pararllel --continue > /dev/null 2>&1 || true


COPY . /build
RUN gradle build -x test --parallel

FROM openjdk:11.0-slim
WORKDIR /app


COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar

EXPOSE 8080

USER nobody
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar",
                "-Dsun.net.inetaddr.ttl=0", "app.jar"]




