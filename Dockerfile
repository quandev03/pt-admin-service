FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# copy wrapper trước
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# 🔧 Fix EOL + cấp quyền thực thi cho mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# tải dependency để cache layer
RUN ./mvnw -B -DskipTests dependency:go-offline

# copy source và build
COPY src/ src/
RUN ./mvnw -B -DskipTests package