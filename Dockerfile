FROM maven:3.9.11-eclipse-temurin-25-noble AS build
WORKDIR /app
ARG GITHUB_USER
ARG GITHUB_TOKEN
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/
COPY pom.xml .
COPY src ./src
RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN mvn clean package -B

FROM eclipse-temurin:25-jdk-ubi10-minimal
WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

ENV SERVER_PORT="8080"
ENV MODEL_HOST="http://localhost:8081"

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]
