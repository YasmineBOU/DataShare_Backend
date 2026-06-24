# Étape 1 : Construction du JAR
FROM amazoncorretto:25-alpine AS builder
WORKDIR /app
RUN apk add --no-cache maven
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Exécution
FROM amazoncorretto:25-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# On expose le port
EXPOSE 8080

# --- LOGIQUE AUTOMATIQUE AU DÉMARRAGE ---
# Si le fichier .p12 n'existe pas dans le dossier ssl/, on le génère à la volée avant de lancer l'app Java
ENTRYPOINT ["/bin/sh", "-c", "\
mkdir -p ssl && \
if [ ! -f ssl/datashare-dev.p12 ]; then \
  echo 'Génération automatique du keystore SSL...'; \
  keytool -genkeypair \
    -alias ${SERVER_SSL_KEY_ALIAS:-datashare} \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore ssl/datashare-dev.p12 \
    -storepass ${SERVER_SSL_KEY_STORE_PASSWORD:-password} \
    -keypass ${SERVER_SSL_KEY_STORE_PASSWORD:-password} \
    -validity 365 \
    -dname 'CN=localhost,OU=DataShare,O=OpenClassrooms,L=Paris,S=IDF,C=FR' \
    -ext SAN=dns:localhost,ip:127.0.0.1; \
fi && \
java -jar app.jar \
"]