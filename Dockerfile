FROM gradle:4.2.1-jdk8-alpine
LABEL maintainer="jaroslaw.michalik.rozkmin@gmail.com"

WORKDIR /app

ENV PORT=8080
ENV REFRESH=5000

RUN whoami

RUN ls -la

USER root
RUN chown -R gradle /app

COPY . /app
RUN ls -la

RUN ./gradlew stage
CMD ./gradlew run