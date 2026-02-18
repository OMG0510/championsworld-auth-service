FROM maven:3.9.9-amazoncorretto-21 AS builder

WORKDIR /app
COPY pom.xml .
# Download all dependencies (this will be cached unless your pom.xml changes)
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]





