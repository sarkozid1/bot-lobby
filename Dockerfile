# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# A legtobb PaaS (pl. Railway) a PORT env valtozoban adja meg, melyik porton
# kell figyelni - server.port ezt olvassa (lasd application.yml).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
