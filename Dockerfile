FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# ARG GITHUB_USER
# ARG GITHUB_TOKEN

# RUN mkdir -p /root/.m2
# COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

# RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN mvn -B -q dependency:go-offline
RUN mkdir -p /root/.m2
RUN --mount=type=secret,id=maven_settings,target=~/.m2/settings.xml mvn -B -q dependency:go-offline


COPY src ./src
RUN --mount=type=secret,id=maven_settings,target=~/.m2/settings.xml mvn clean package -B


FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

ENV SERVER_PORT="8080"
ENV MODEL_HOST="http://model-service:8081"

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]
