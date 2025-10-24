FROM openjdk:25-slim-bookworm
# Use the appropriate Java version for your app

# Install necessary dependencies
RUN apt-get update && apt-get install -y \
    libc++-dev

# Create app directory
WORKDIR /app

# Copy the JAR file
COPY target/yugram-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy the native library
COPY libs/libtdjni.so /app/libs/libtdjni.so

# Set library path permissions
RUN chmod 644 /app/libs/libtdjni.so

# Expose port
EXPOSE 8080

# Run the application with java.library.path
ENTRYPOINT ["java", "-Djava.library.path=/app/libs", "-jar", "/app/app.jar"]
