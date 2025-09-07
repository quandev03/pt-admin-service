# ====== Stage 1: Build (Maven + JDK 17) ======
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 1) Tối ưu cache: copy pom + mvnw + .mvn trước
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# 2) Chuẩn hoá mvnw khi commit từ Windows, rồi cấp quyền chạy
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# 3) Nạp settings.xml private (được Jenkins materialize ở ci/settings.xml)
#    -> chứa mirror/credentials để tải com.vnsky:vnsky-excel:0.4.5-SNAPSHOT
COPY ci/settings.xml /root/.m2/settings.xml

# 4) Tải dependencies để tận dụng cache layer
RUN ./mvnw -B -DskipTests dependency:go-offline

# 5) Copy mã nguồn và build
COPY src/ src/
RUN ./mvnw -B -DskipTests package

# ====== Stage 2: Runtime (JRE 17) ======
FROM eclipse-temurin:17-jre-jammy
# Tạo user không phải root để chạy an toàn
RUN useradd -ms /bin/bash appuser
WORKDIR /app

# Copy file jar đã build từ stage trước
# (Sử dụng wildcard để không cần biết chính xác artifactId/finalName)
COPY --from=builder /app/target/*.jar /app/app.jar

# Port ứng dụng (đồng bộ với docker-compose.yml)
EXPOSE 8081

# Tuỳ chọn: timezone (nếu cần)
# RUN ln -snf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && echo "Asia/Ho_Chi_Minh" > /etc/timezone

# Biến runtime phổ biến
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod

# HEALTHCHECK (nếu app có actuator; nếu không dùng thì comment lại)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8081/actuator/health | grep -q '"status":"UP"' || exit 1

# Quyền sở hữu & chạy bằng non-root
RUN chown -R appuser:appuser /app
USER appuser

# Chạy app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]