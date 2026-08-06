# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven wrapper and pom first to leverage layer caching
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN chmod +x mvnw

# Prime dependency cache
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build the Spring Boot jar
COPY src src
RUN ./mvnw -q -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the packaged application jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

