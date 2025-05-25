FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk update && \
    apk add --no-cache chromium && \
    apk add --no-cache chromium-chromedriver

ENV PYTHONUNBUFFERED=1
RUN apk add --update --no-cache python3 && ln -sf python3 /usr/bin/python
RUN apk add --no-cache py3-setuptools && apk add --no-cache py3-pip
RUN mkdir -p target && mkdir -p python

COPY python python
COPY target/*.jar target/app.jar
COPY prod.env target/prod.env

ENTRYPOINT ["java", "-jar", "target/app.jar"]