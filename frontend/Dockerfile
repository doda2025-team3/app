FROM maven:3.9.11-eclipse-temurin-25-noble AS build
WORKDIR /app
COPY frontend/pom.xml .
COPY frontend/src ./src
RUN mvn clean package

FROM eclipse-temurin:25-jdk-ubi10-minimal
WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

ENV APP_PORT="8080"
ENV MODEL_HOST="http://localhost:8081"

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]