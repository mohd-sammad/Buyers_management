# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven files first (to cache dependencies)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Fix line endings for Windows and make mvnw executable
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application (skip tests to save time)
RUN ./mvnw clean package -DskipTests

# Expose the port Spring Boot uses
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/buyer-management-1.0.0.jar"]
