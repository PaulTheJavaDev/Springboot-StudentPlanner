FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Kopiere pom.xml
COPY pom.xml .

# Erstelle die richtige Maven-Struktur
RUN mkdir -p src/main/java

# Kopiere alle Java-Packages
COPY de ./src/main/java/de

# Build
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]