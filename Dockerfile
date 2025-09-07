# Dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# cho phép mvnw chạy trên Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# settings.xml sẽ được Jenkins đặt vào /workspace/ci/settings.xml
COPY ci/settings.xml /root/.m2/settings.xml

# preload deps (tùy chọn)
RUN ./mvnw -B -DskipTests dependency:go-offline || true

# build
COPY src/ src/
RUN ./mvnw -B -DskipTests package

# (nếu chạy fat-jar thì thêm stage runtime ở dưới)