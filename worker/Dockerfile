FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar /app/app.jar

RUN chmod +x /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
