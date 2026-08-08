# RADAR-Appserver Jersey

Monolith deployment of the RADAR-Appserver using Jersey (JAX-RS) and Hibernate. This replaces the original Spring Boot application with the same functionality and database schema.

## Running

```bash
# Build
./gradlew :appserver-jersey:build

# Run
./gradlew :appserver-jersey:run
```

The server starts at `http://localhost:8080/` by default.

## Configuration

Configuration is in `src/main/resources/appserver.yml`:

```yaml
server:
  baseUri: http://0.0.0.0:8080/

auth:
  managementPortalUrl: http://localhost:8081/managementportal
  resourceName: res_AppServer

db:
  jdbcUrl: jdbc:postgresql://localhost:5432/appserver
  username: radar
  password: radar

email:
  enabled: false
```

Environment variables can override config values (e.g., `APPSERVER_JDBC_URL`).

## Database

Uses a single PostgreSQL database (`appserver`). Schema is managed by Liquibase changelogs in `src/main/resources/db/changelog/`.

The changelogs include all 20 original changesets from the Spring Boot era, so migrating from the original app to Jersey is an in-place operation (Liquibase only applies new changesets).

## API Documentation

OpenAPI spec and Swagger UI are available at runtime:
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI YAML: `http://localhost:8080/openapi.yaml`
- OpenAPI JSON: `http://localhost:8080/openapi.json`

## Docker

```bash
docker build -t radar-appserver .
docker run -p 8080:8080 \
  -e GOOGLE_APPLICATION_CREDENTIALS=/etc/google-credentials.json \
  -v ./google-credentials.json:/etc/google-credentials.json \
  radar-appserver
```
