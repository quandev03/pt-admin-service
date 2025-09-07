# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Maven settings (Jenkinsfile đã tạo sẵn settings.xml ở root)
COPY settings.xml /root/.m2/settings.xml

# Maven wrapper & dependencies cache
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

# Source & package
COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]