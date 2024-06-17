# Generate Build
FROM maven:3-eclipse-temurin-17-alpine as build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Compile and package the application
RUN mvn clean package -Dmaven.test.skip=true

CMD ["mvn", "spring-boot:run"]