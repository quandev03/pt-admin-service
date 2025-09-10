# ===== STAGE 1: BUILD =====
FROM maven:3.9-eclipse-temurin-21 AS build

# Sao chép settings Maven (đổi tên file của bạn ở đây)
COPY settings-vnsky.xml /root/.m2/settings.xml

# Tối ưu cache: copy pom trước, tải dependencies
WORKDIR /workspace
COPY pom.xml .
RUN mvn -s /root/.m2/settings.xml -e -B -U -DskipTests dependency:go-offline

# Copy source và build
COPY src ./src
RUN mvn -s /root/.m2/settings.xml -e -B -DskipTests package

# ===== STAGE 2: RUNTIME =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Lấy file jar đã build
COPY --from=build /workspace/target/*.jar app.jar

# Múi giờ VN (tuỳ chọn)
ENV TZ=Asia/Ho_Chi_Minh

# Bạn có thể truyền JAVA_OPTS qua docker compose (.env)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]