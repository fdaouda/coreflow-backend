# --- Étape 1 : Build de l'application avec Maven ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copie des fichiers du projet
COPY pom.xml .
COPY src ./src

# Compilation et création du fichier JAR (sans relancer les tests déjà passés)
RUN mvn clean package -DskipTests

# --- Étape 2 : Image finale légère pour l'exécution ---
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp

# Copie du JAR généré dans l'étape 1
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
