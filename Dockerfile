FROM maven:3.9.11-eclipse-temurin-25-noble AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY settings.xml ./settings.xml
RUN --mount=type=secret,id=GITHUB_TOKEN \
    export GITHUB_TOKEN=$(cat /run/secrets/GITHUB_TOKEN) && \
    export GITHUB_USERNAME=${GITHUB_USERNAME:-${GITHUB_ACTOR:-lynlynnie}} && \
    mvn -B -s settings.xml clean package -DskipTests

FROM eclipse-temurin:25-jdk-ubi10-minimal
WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

ENV APP_PORT="8080"
ENV MODEL_HOST="http://localhost:8081"

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]