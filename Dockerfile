FROM openjdk:8
ENV APP_NAME docker-spring-boot
COPY ./target/${APP_NAME}.jar  /app/${APP_NAME}.jar
WORKDIR /app
CMD java -jar ${APP_NAME}.jar
EXPOSE 8080