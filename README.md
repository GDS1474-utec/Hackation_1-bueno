# TropelCare Signal Engine - Checkpoint 1 Base

Backend base en Spring Boot para la hackathon. Esta versión cubre el **Checkpoint 1: Mundo Base de Tropeles**.

## Integrantes

- Dávila Segovia, Giancarlo (202510521)
- Loo Leon, Tommy Bryan (202510241)
- Cussianovich Monti, Alejandra (202510391)

## Tecnologías

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Bean Validation

## Levantar PostgreSQL

```bash
docker run --name tropelcare-db \
  -e POSTGRES_DB=tropelcare \
  -e POSTGRES_USER=tropeluser \
  -e POSTGRES_PASSWORD=tropelpass \
  -p 5432:5432 \
  -d postgres:16
```

Si el contenedor ya existe:

```bash
docker start tropelcare-db
```

## Variables de entorno requeridas

Crear un archivo `.env` local o configurar variables del sistema. No subir `.env` al repositorio.

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tropelcare
DB_USERNAME=tropeluser
DB_PASSWORD=tropelpass

ADMIN_NAME=Cameron Walker
ADMIN_EMAIL=cameron@tuckersoft.com
ADMIN_NOTIFICATION_EMAIL=equipo@example.com
```

## Ejecutar la app

Con Maven instalado:

```bash
mvn spring-boot:run
```

Si agregan Maven Wrapper al proyecto:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

## Endpoints de Checkpoint 1

### Guardianes

```http
GET /api/v1/guardians
GET /api/v1/guardians/{id}
```

Al arrancar la aplicación, `DataInitializer` crea automáticamente a Cameron Walker si no existe.

### Sectores

```http
POST /api/v1/sectors
GET /api/v1/sectors
GET /api/v1/sectors/{id}
```

Body:

```json
{
  "sectorCode": "SECTOR-7",
  "climate": "RETRO_ARCADE",
  "capacity": 1
}
```

El sector inicia con:

```json
{
  "currentLoad": 0,
  "stabilityLevel": 100
}
```

### Tropeles

```http
POST /api/v1/tropels
GET /api/v1/tropels/{id}
GET /api/v1/tropels?vitalState=ESTABLE&page=0&size=10
```

Body:

```json
{
  "name": "BipBop",
  "species": "GLITCHY",
  "sectorId": 1,
  "guardianId": 1
}
```

El Tropel inicia con:

```json
{
  "vitalState": "ESTABLE",
  "energyLevel": 80,
  "chaosIndex": 10,
  "mutationStage": 0
}
```

Al crear un Tropel, el `currentLoad` del sector sube en 1. Si el sector está lleno, el endpoint devuelve 400.

## Correr tests

```bash
mvn test
```

O con Maven Wrapper:

```bash
./mvnw test
```

## Flujo asíncrono

Todavía no está implementado en esta base. Se agregará desde el Checkpoint 3 con `@Async` y `@TransactionalEventListener(phase = AFTER_COMMIT)`.
