# Etapa de construcción con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (caché de Maven)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Configurar variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker

# Crear directorio para logs
RUN mkdir -p /app/logs

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]