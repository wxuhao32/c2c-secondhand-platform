# 使用 Maven 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建数据目录
RUN mkdir -p /data

# 复制构建好的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 10000

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=10000
ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
