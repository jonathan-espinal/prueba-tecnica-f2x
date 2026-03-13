# Dockerización del Proyecto Spring Boot con PostgreSQL

Este proyecto ha sido configurado para ejecutarse en Docker con PostgreSQL.

## Requisitos

- Docker 20.10+ y Docker Compose
- Java 21 (solo para desarrollo local)

## Estructura de Archivos

- `Dockerfile`: Configuración para construir la imagen de la aplicación
- `docker-compose.yml`: Orquestación de servicios (PostgreSQL + App)
- `src/main/resources/application-docker.properties`: Configuración para entorno Docker
- `.dockerignore`: Archivos excluidos del contexto de Docker

## Configuración

### Variables de Entorno (PostgreSQL)

- `POSTGRES_DB`: prueba_db
- `POSTGRES_USER`: prueba_user
- `POSTGRES_PASSWORD`: prueba_password

### Variables de Entorno (Aplicación)

- `SPRING_DATASOURCE_URL`: jdbc:postgresql://postgres:5432/prueba_db
- `SPRING_DATASOURCE_USERNAME`: prueba_user
- `SPRING_DATASOURCE_PASSWORD`: prueba_password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: update
- `SPRING_JPA_SHOW_SQL`: true

## Comandos Docker

### Construir y ejecutar todos los servicios
```bash
docker-compose up --build
```

### Ejecutar en segundo plano
```bash
docker-compose up -d
```

### Detener servicios
```bash
docker-compose down
```

### Detener servicios y eliminar volúmenes
```bash
docker-compose down -v
```

### Ver logs de la aplicación
```bash
docker-compose logs app
```

### Ver logs de PostgreSQL
```bash
docker-compose logs postgres
```

### Acceder a la base de datos PostgreSQL
```bash
docker-compose exec postgres psql -U prueba_user -d prueba_db
```

### Reconstruir solo la aplicación
```bash
docker-compose build app
```

## Puertos

- **Aplicación Spring Boot**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## Desarrollo Local vs Docker

### Desarrollo Local (SQLite)
- Usa `application.properties` con configuración SQLite
- Base de datos: `prueba.db` (archivo local)
- No requiere Docker

### Docker (PostgreSQL)
- Usa `application-docker.properties`
- Base de datos PostgreSQL en contenedor
- Datos persistentes en volumen Docker

## Migración de Datos

Si necesitas migrar datos de SQLite a PostgreSQL:

1. Exportar datos de SQLite:
```bash
sqlite3 prueba.db .dump > backup.sql
```

2. Adaptar el archivo SQL para PostgreSQL
3. Importar a PostgreSQL:
```bash
docker-compose exec -T postgres psql -U prueba_user -d prueba_db < backup_adapted.sql
```

## Solución de Problemas

### La aplicación no se conecta a PostgreSQL

1. Verificar que PostgreSQL esté saludable:
```bash
docker-compose ps
```

2. Verificar logs de PostgreSQL:
```bash
docker-compose logs postgres
```

3. Verificar conectividad:
```bash
docker-compose exec postgres pg_isready -U prueba_user -d prueba_db
```

### Error de permisos en volumen de logs

Si hay errores de permisos al escribir logs:
```bash
mkdir -p logs
chmod 777 logs
```

### Reconstruir desde cero
```bash
docker-compose down -v
sudo rm -rf logs/
docker-compose up --build
```
