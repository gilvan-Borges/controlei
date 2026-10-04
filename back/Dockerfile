# Build stage
FROM maven:3.9.11-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy shared-events and install it in local Maven cache
COPY shared-events ./shared-events
RUN mvn -f shared-events/pom.xml clean install -DskipTests -B

# Copy root pom and src
COPY pom.xml .
COPY src ./src

# Build package without tests
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

# Heap proporcional ao limite do container (o padrao da JVM em container pequeno e conservador demais) e saida
# imediata em OutOfMemoryError, para o Compose reiniciar o servico em vez de deixa-lo meio morto.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
