FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# copy tối thiểu để cache tốt
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/

# đảm bảo mvnw chạy được trên Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# settings.xml (được cung cấp bởi Jenkins vào workspace)
COPY settings.xml /root/.m2/settings.xml

# tuỳ chọn: preload deps để cache layer (có thể bỏ)
RUN ./mvnw -B -DskipTests dependency:go-offline || true

# build
COPY src/ src/
RUN ./mvnw -B -DskipTests package

# --- (nếu bạn chạy kiểu fat jar) ---
# FROM eclipse-temurin:17-jre as runtime
# COPY --from=builder /app/target/*-SNAPSHOT.jar /app/app.jar
# EXPOSE 8080
# ENTRYPOINT ["java","-jar","/app/app.jar"]