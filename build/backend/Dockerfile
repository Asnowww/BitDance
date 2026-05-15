# BitDance backend Dockerfile（多阶段构建：Maven 编译 + Eclipse Temurin 21 运行）
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/bitdance-backend.jar /app/app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -Xms512m -Xmx1g"
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
