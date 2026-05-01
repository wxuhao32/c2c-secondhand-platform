FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN mkdir -p /data/logs /data/uploads

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 10000

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=10000
ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/data/logs/", "-jar", "app.jar"]
