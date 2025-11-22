# ===== STAGE 1: BUILD =====
FROM maven:3.9-eclipse-temurin-17 AS build

ARG MVN_ARGS="-s /root/.m2/settings.xml -e -B -DskipTests -ntp"
ENV MAVEN_CONFIG=/root/.m2

# Copy Maven settings
COPY settings-vnsky.xml ${MAVEN_CONFIG}/settings.xml

WORKDIR /workspace

# Copy Maven wrapper (if exists)
COPY mvnw* .mvn/ ./
RUN chmod +x mvnw 2>/dev/null || true

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached layer - only rebuilds if pom.xml changes)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn ${MVN_ARGS} dependency:go-offline || \
    ./mvnw ${MVN_ARGS} dependency:go-offline

# Copy source code (separate layer - only rebuilds if source changes)
COPY src ./src

# Build application (cached layer - only rebuilds if source or dependencies change)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn ${MVN_ARGS} package || \
    ./mvnw ${MVN_ARGS} package

# ===== STAGE 2: RUNTIME =====
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR file from build stage
COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

# Set timezone (single layer)
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*

# Switch to non-root user
USER spring:spring

# JVM optimization flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]