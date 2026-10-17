# ---------- Etapa 1: construir el .jar (una "cocina" con Maven y el JDK) ----------
FROM maven:3.9-eclipse-temurin-17 AS construccion
WORKDIR /app
COPY pom.xml .
COPY src ./src
# -DskipTests: las pruebas se corren en tu computadora; aquí no existen tus variables.
RUN mvn -B -DskipTests clean package

# ---------- Etapa 2: solo lo necesario para ejecutar (el "plato servido") ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=construccion /app/target/*.jar app.jar
EXPOSE 8080
# MaxRAMPercentage: deja que Java use hasta el 75% de los 512 MB del plan gratuito.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
