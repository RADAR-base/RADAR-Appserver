# RADAR-Appserver Microservices

Decomposed deployment of the RADAR-Appserver. Each service runs independently with its own database and communicates via HTTP contracts.

## Services

| Service                     | Port | Database                    |
|-----------------------------|------|-----------------------------|
| **gateway-service**         | 8080 | —                           |
| **project-service**         | 9010 | `appserver_project`         |
| **github-service**          | 9011 | —                           |
| **protocol-service**        | 9012 | —                           |
| **user-service**            | 9013 | `appserver_user`            |
| **task-service**            | 9014 | `appserver_task`            |
| **cloud-messaging-service** | 9015 | `appserver_cloud_messaging` |

## Running

### With Docker Compose

```bash
cd microservices
docker-compose up -d
```

This starts all services and their PostgreSQL instances.

### Locally (individual service)

```bash
# Start databases first
docker-compose up -d project-service-db user-service-db task-service-db cloud-messaging-service-db

# Run a service
./gradlew :microservices:project-service:run
```

### Build All

```bash
./gradlew :microservices:build
```

## API Documentation

The gateway service exposes the public API at `http://localhost:8080`. Each service exposes Swagger UI and OpenAPI specs:

| Service                     | Swagger UI                      | OpenAPI spec                         |
|-----------------------------|---------------------------------|--------------------------------------|
| **gateway-service**         | `http://localhost:8080/swagger` | `http://localhost:8080/openapi.yaml` |
| **project-service**         | `http://localhost:9010/swagger` | `http://localhost:9010/openapi.yaml` |
| **github-service**          | `http://localhost:9011/swagger` | `http://localhost:9011/openapi.yaml` |
| **protocol-service**        | `http://localhost:9012/swagger` | `http://localhost:9012/openapi.yaml` |
| **user-service**            | `http://localhost:9013/swagger` | `http://localhost:9013/openapi.yaml` |
| **task-service**            | `http://localhost:9014/swagger` | `http://localhost:9014/openapi.yaml` |
| **cloud-messaging-service** | `http://localhost:9015/swagger` | `http://localhost:9015/openapi.yaml` |

JSON specs are also available at `/openapi.json`.

## Inter-Service Communication

Services communicate via HTTP using contracts defined in the `contract` module:

```
gateway → project-service
gateway → user-service
gateway → task-service
gateway → cloud-messaging-service
gateway → protocol-service
gateway → github-service
user-service → project-service
protocol-service → github-service
```

## Integration Tests

```bash
./gradlew :microservices:integration-tests:integrationTest
```

This spins up the full stack via Docker Compose and runs integration tests.
