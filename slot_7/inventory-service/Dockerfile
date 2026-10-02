# ---- Stage 1: build jar bang Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Tai dependency truoc de tan dung cache layer khi chi sua code
RUN mvn -q dependency:go-offline
COPY src ./src
# Bo qua test vi integration test can Docker (Testcontainers)
RUN mvn -q package -DskipTests

# ---- Stage 2: image chay app, chi can JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
