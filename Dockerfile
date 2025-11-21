ARG GITHUB_USER
ARG GITHUB_TOKEN

FROM maven:3.9.11-eclipse-temurin-25-noble AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY settings.xml /root/.m2/settings.xml
RUN mvn -s /root/.m2/settings.xml clean package

FROM eclipse-temurin:25-jdk-ubi10-minimal
WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

ENV APP_PORT="8080"
ENV MODEL_HOST="http://localhost:8081"

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]