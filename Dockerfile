FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk update && \
    apk add --no-cache chromium && \
    apk add --no-cache chromium-chromedriver

COPY target/*.jar target/app.jar
COPY prod.env target/prod.env

ENTRYPOINT ["java", "-jar", "target/app.jar"]