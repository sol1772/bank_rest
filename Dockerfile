# Stage 1: Build
FROM maven:3.9.14-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml has not changed)
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy only the built JAR from the first stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port and run
EXPOSE 8080

# --enable-preview is required when the jar was compiled with JDK 25 preview features
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
