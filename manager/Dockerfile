FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar /app/app.jar
EXPOSE 8080
RUN apt-get update && apt-get install -y curl

RUN chmod +x /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
