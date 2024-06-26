# Use Eclipse Temurin JDK as parent image
FROM eclipse-temurin:21

# Listen on port 8080
EXPOSE 8080

# Set the working directory in the container
WORKDIR /opt/app

# Copy the Maven wrapper and POM file
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Install the dependencies
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Compile and run the application
ENTRYPOINT ["./mvnw", "spring-boot:run", "-Pdocker"]