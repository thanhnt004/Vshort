
#Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Nhận tên service cần build từ docker-compose
ARG SERVICE_NAME
#    Layer này chỉ thay đổi khi pom.xml thay đổi
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY config-service/pom.xml config-service/
COPY discovery-service/pom.xml discovery-service/
COPY api-gateway/pom.xml api-gateway/
COPY user-identity-service/pom.xml user-identity-service/
COPY notification-service/pom.xml notification-service/
COPY user-profile-service/pom.xml user-profile-service/
# Tải dependencies với BuildKit cache mount
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B --fail-never

# Copy CHỈ source code của common-lib (dependency chung) và service cần build để tối ưu cache
COPY common-lib/src common-lib/src
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src

# Build module cụ thể với Maven cache mount
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# Runtime
FROM eclipse-temurin:25-jre-alpine

# Cài curl để phục vụ Docker Healthcheck
RUN apk add --no-cache curl

WORKDIR /app
ARG SERVICE_NAME

# Copy file jar từ kết quả build phía trên
COPY --from=build /app/${SERVICE_NAME}/target/*.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]