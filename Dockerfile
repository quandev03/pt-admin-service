# syntax=docker/dockerfile:1.7

ARG BASE_BUILDER="maven:3.9.9-eclipse-temurin-17"
ARG BASE_RUNNER="eclipse-temurin:17-jre-jammy"

FROM ${BASE_BUILDER} AS builder
WORKDIR /app

# copy pom.xml và mvnw để cache dependencies
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# Nếu có settings.xml từ Jenkins secret → mount bằng BuildKit
RUN --mount=type=secret,id=mvnsettings,target=/root/.m2/settings.xml \
    ./mvnw -ntp -B -DskipTests dependency:go-offline

# copy source và build
COPY src/ src/
RUN --mount=type=secret,id=mvnsettings,target=/root/.m2/settings.xml \
    ./mvnw -ntp -B -DskipTests clean package

FROM ${BASE_RUNNER} AS runner
WORKDIR /opt/app

COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar

USER 1000
EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]