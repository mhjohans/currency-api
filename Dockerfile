# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and POM file
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Install the dependencies
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Compile and run the application
ENTRYPOINT ["./mvnw", "spring-boot:run"]