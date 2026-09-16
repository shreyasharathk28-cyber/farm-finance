FROM eclipse-temurin:21-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run the application - using the actual JAR name
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/farm-finance-0.0.1-SNAPSHOT.jar"]