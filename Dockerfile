# 1. Use an official Java runtime as a parent image
# (Ensure this matches the Java version in your build.gradle, e.g., 17 or 21)
FROM eclipse-temurin:21-jdk-alpine

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the jar file from your host machine into the container
# We rename it to 'app.jar' to make running it easier
COPY build/libs/*.jar app.jar

# 4. Expose the port your app runs on (Spring Boot defaults to 8080)
EXPOSE 8080

# 5. Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]