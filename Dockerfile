# syntax=docker/dockerfile:1
# escape=`


# Stage 1: Build
FROM maven:3.9 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests


# Stage 2: Production
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/static /app/src/main/resources/static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
