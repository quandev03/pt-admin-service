ARG BASE_BUILDER="harbor.vissoft.vn/vnsky/java-builder"
ARG BASE_RUNNER="harbor.vissoft.vn/vnsky/java-runner"

FROM $BASE_BUILDER AS builder
LABEL org.opencontainers.image.authors="doanhcd"
WORKDIR /app
ENV TZ=Asia/Ho_Chi_Minh
COPY ./pom.xml ./pom.xml
ARG MVN_REMOTE_REPO="https://repo1.maven.org/maven2/"
RUN echo "Using maven repo: $MVN_REMOTE_REPO" && \
    mvn -B dependency:go-offline
COPY . /app
RUN mvn clean package -DskipTests=true


FROM $BASE_RUNNER AS runner
LABEL org.opencontainers.image.authors="doanhcd"
WORKDIR /app
ENV TZ=Asia/Ho_Chi_Minh

COPY --from=builder /app/target/admin-service-0.0.1-SNAPSHOT.jar /opt
RUN adduser -r -m admin-service && \
    chmod +x /opt/admin-service-0.0.1-SNAPSHOT.jar && \
    chown -R admin-service:admin-service /app
USER admin-service

ENTRYPOINT ["java"]
CMD ["-jar", "/opt/admin-service-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080
