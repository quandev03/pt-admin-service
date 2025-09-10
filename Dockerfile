# ===== STAGE 1: BUILD =====
FROM maven:3.9-eclipse-temurin-21 AS build

FROM maven:3.9-eclipse-temurin-17 AS build

COPY settings-vnsky.xml /root/.m2/settings.xml

WORKDIR /workspace
COPY pom.xml .
RUN mvn -s /root/.m2/settings.xml -e -B -U -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -s /root/.m2/settings.xml -e -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

ENV TZ=Asia/Ho_Chi_Minh

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]