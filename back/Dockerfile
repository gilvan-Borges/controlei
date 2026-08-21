# Build stage
FROM maven:3.9.11-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy source code and POM
COPY pom.xml .
COPY src ./src

# Build package without tests (tests are verified prior to build)
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S controlei && adduser -S controlei -G controlei
USER controlei:controlei

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
