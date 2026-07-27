# RADAR-Appserver

General purpose application server for the RADAR platform with capability to schedule push notifications via Firebase Cloud Messaging.

This project has two deployment modes:
- **appserver-jersey** — monolith (single service, single database)
- **microservices** — decomposed into independent services with separate databases

<!-- TOC -->
* [RADAR-Appserver](#radar-appserver)
  * [Introduction](#introduction)
  * [Getting Started](#getting-started)
  * [REST API](#rest-api)
    * [Quickstart](#quickstart)
  * [FCM](#fcm)
    * [AdminSDK](#adminsdk)
  * [Docker / Docker Compose](#docker--docker-compose)
  * [Architecture](#architecture)
  * [Notification Lifecycle](#notification-lifecycle)
  * [Protocols](#protocols)
  * [Email Notifications](#email-notifications)
  * [Documentation](#documentation)
  * [Security](#security)
    * [Management Portal](#management-portal)
    * [Management Portal Clients](#management-portal-clients)
    * [Other Security Providers](#other-security-providers)
  * [Database Migrations](#database-migrations)
  * [Code Quality and Testing](#code-quality-and-testing)
  * [Monitoring](#monitoring)
    * [Sentry](#sentry)
  * [Current Features](#current-features)
<!-- TOC -->

## Introduction

This is an app server which provides facilities to store app information (User related) and scheduling of push
notifications using Firebase Cloud Messaging.

This is specifically developed to support the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire)
application but can be easily extended or modified to suit the needs of different applications.

The app server provides REST endpoints to interact with the entities and data. For detailed info on the REST API please
see the relevant section below.

## Getting Started

1. First you will need to create a Firebase project for your application and add it to your app. This will give you
   access to all the Firebase services. Follow the instructions on
   the [official docs](https://firebase.google.com/docs/) according to your platform.

2. The AppServer needs a database. You can use either a standalone PostgreSQL instance or an in-memory H2 database.

   **Option A: Standalone PostgreSQL** (recommended for production)

   Start a PostgreSQL instance with Docker:
   ```bash
   docker run -d --name appserver-db -p 5432:5432 \
     -e POSTGRES_DB=appserver -e POSTGRES_USER=radar -e POSTGRES_PASSWORD=radar \
     postgres:15
   ```

   **Option B: In-memory H2 database** (for development only, not recommended for production)

   In `appserver-jersey/src/main/resources/appserver.yml`, uncomment the H2 configuration:
   ```yaml
   database:
     jdbcDriver: org.h2.Driver
     jdbcUrl: jdbc:h2:mem:dev
     hibernateDialect: org.hibernate.dialect.H2Dialect
   ```

3. Build the project (requires Java 17):
   ```bash
   # Monolith
   ./gradlew :appserver-jersey:build

   # Microservices
   ./gradlew :microservices:build
   ```

4. Run the monolith:
   ```bash
   ./gradlew :appserver-jersey:run
   ```
   The server starts at `http://localhost:8080/`.

5. The AppServer uses the Admin SDK to communicate with Firebase Cloud Messaging. To configure this, see the [FCM section](#fcm).

6. For microservices deployment, see [microservices/README.md](microservices/README.md).

## REST API

The full API specification is available via OpenAPI/Swagger when you launch the app server:
- OpenAPI spec: `http://localhost:8080/openapi.yaml` or `http://localhost:8080/openapi.json`

1. Create a project. If using Management Portal, this should be exactly same as the project name
   in management portal.
    ```
   POST http://localhost:8080/projects/p1
   {
    "projectId": "p1"
   }
   ```
2. Create a user. Make sure to use the correct FCM token otherwise you will not receive the
   notification on the device.
    ```
   POST http://localhost:8080/projects/p1/users/u2
   {
    "subjectId": "u2",
    "fcmToken" : "shdzdxcvc",
    "enrolmentDate": "2018-11-29T00:00:00Z",
    "timezone": "Australia/Sydney",
    "language": "en"
    }
   ```
3. Create a notification.
    ```
    POST http://localhost:8080/projects/p1/users/u2/messaging/notifications
    {
        "title" : "Questionnaire Time",
        "body": "Urgent Questionnaire Pending. Please complete now.",
        "ttlSeconds": 0,
        "sourceId": "null",
        "type": "ers",
        "sourceType": "aRMT",
        "appPackage": "org.phidatalab.radar_armt",
        "scheduledTime": "2022-02-23T09:04:00Z"
    }
    ```

### Quickstart

1. Run the AppServer by following the steps in [Getting Started](#getting-started).

2. Create a new Project by making a `POST` request to `http://localhost:8080/projects/{projectId}`:
   ```json
   {
     "projectId": "radar"
   }
   ```

3. Create a new User in the Project by making a `POST` request to
   `http://localhost:8080/projects/radar/users/sub-1`:
   ```json
   {
     "subjectId": "sub-1",
     "fcmToken": "get-this-from-the-device",
     "enrolmentDate": "2019-07-29T00:00:00Z",
     "timezone": "Europe/London",
     "language": "en"
   }
   ```
   **Note:** You will need to get the FCM token from the device and the app. See
   the [FCM setup info](https://firebase.google.com/docs/cloud-messaging) for your platform.

4. Schedule a notification for the user by making a `POST` request to
   `http://localhost:8080/projects/radar/users/sub-1/messaging/notifications`:
   ```json
   {
     "title": "Test Title",
     "body": "Test Body",
     "ttlSeconds": 86400,
     "sourceId": "z",
     "type": "ESM",
     "sourceType": "aRMT",
     "appPackage": "aRMT",
     "scheduledTime": "2025-06-29T15:25:58.054Z"
   }
   ```
   Update the `scheduledTime` to the desired time of notification delivery.

5. You will receive a notification at the `scheduledTime` on the device associated with the FCM token.
   Explore other features via the OpenAPI spec — see [Documentation](#documentation).

## FCM

### AdminSDK

To configure AdminSDK, follow the official
Firebase [documentation](https://firebase.google.com/docs/admin/setup#initialize-sdk) till you setup the environment
variable (`GOOGLE_APPLICATION_CREDENTIALS`). In the properties
file, you would need to set `fcmserver.fcmsender` to `org.radarbase.fcm.downstream.AdminSdkFcmSender`.

## Docker / Docker Compose

The AppServer is available as a Docker container.

```shell
docker run -v /logs/:/var/log/radar/appserver/ \
  -v etc/google-credentials.json:/etc/google-credentials.json \
  -e "GOOGLE_APPLICATION_CREDENTIALS=/etc/google-credentials.json" \
  radarbase/radar-appserver:2.4.3
```

Make sure to have the correct path to the `google-credentials.json` file.

The same can be achieved by running as a Docker Compose service. Specify the following in your `docker-compose.yml`:

```yml
services:
  appserver:
    image: radarbase/radar-appserver:2.4.3
    restart: always
    ports:
      - 8080:8080
    volumes:
      - ./logs/:/var/log/radar/appserver/
      - ./etc/google-credentials.json:/etc/google-credentials.json
    environment:
      GOOGLE_APPLICATION_CREDENTIALS: /etc/google-credentials.json
      JDK_JAVA_OPTIONS: -Xmx4G -Djava.security.egd=file:/dev/./urandom
```

For microservices deployment with Docker Compose, see [microservices/docker-compose.yml](microservices/docker-compose.yml).

## Architecture

Here is a high level architecture and data flow diagram for the AppServer and its example interaction with a Cordova
application (hybrid) like the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire).

```text










             ┌───────────────────┐                                 Downstream
             │Device (Google Play│◀─────────────────────────────────Message                                             .───────────.
             │  Services/Apple   │                                        │                                         _.─'             `──.
             │       IPNS)       │                                        └───────────────────────────────────────,'                     `.
             └────────▲────┬─────┴─────────────────────────────────────┐                                        ,'                         `.
                      │    │                                           │                                       ╱                             ╲
                      │    │                                         XMPP                                     ;                               :
                      │    │                                       Upstream                                   │   Firebase Cloud Messaging    │
                     .┴────▼─.                                     Message───────────────────────────────────▶│            Service            │
                   ,'         `.                                                                              :                               ;
                  ; Native Code :                                                                              ╲                             ╱
                  :(IOS/Android);                                                                               ╲                           ╱
                   ╲           ╱                                                                                 `.                       ,'
                    `▲       ,'                                                                                    `.                   ,'
                     │`─────│                                                                                       ▲`──.           _.─'
                     │      │                                                                                       │    `────────'▲
                     │      │                                                                                       │
                     │      │                                                                                       │              ┃
                     │      │                                                                                       │
                     │      │                                                                                       │              ┃
                     │──────▼.                                                                                    Send
                  ,─'         '─.                                                                              downstream          ┃
                 ╱  Cordova FCM  ╲                                                                             Message at
                ;     Plugin      :                                                                             Scheduled          ┃
                :                 ;                                                                               Time
                 ╲               ╱                                                                                  │              ┃
                  ╲             ╱                                                                                   │            FCM Admin
                   '─▲       ,─'                                                                                    │           SDK (Only
                     │`─────'│                                                                                      │           downstream
                     │       │                                                                                      │           messaging)
                     │       │                                                                                      │
                     │       │                                                                                      │              ┃
                     │       │                                                                                      │
                     │       │                                                                                 ┌────┴───────▼──────┻─────── ▼────┐
                     │       │                                                                                 │                                 │
               ┌─────┴───────▼─────────┐                                                                       │                                 │
               │                       │                                   ┌───────────────────────────────────▶                                 │
               │                       │                           Schedule message                            │                                 │
               │                       │                          for future delivery                          │        New App Server           │
               │                       │                            using HTTP REST                            │                                 │
               │                       ├───────────────────────────────────┘                                   │        (HTTP Protocol)          │
               │  CORDOVA APPLICATION  │                                                                       │  (REST API and FCM Admin SDK)   │
               │                       │                          Get confirmation of ─────────────────────────┤                                 │
               │                       ◀───────────────────────success for each request.                       │                                 │
               │                       │                                                                       │                                 │
               │                       │                                   ┌───────────────────────────────────▶                                 │
               │                       │                                   │                                   │                                 │
               │                       │                       Get/Set user metrics,                           │                                 │
               │                       ├───────────────────schedule, notifications, etc                        │                                 │
               │                       │                                                                       │                                 │
               │                       │                               More                                    │                                 │
               │                       ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ functionality ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ▶                                 │
               └───────────────────────┘                               .....                                   └─────────────────────────────────┘
```

## Notification Lifecycle

The Appserver manages the lifecycle of the Notifications through state change events. It uses Pub/Sub paradigm so
other subscribers can also hook up to the Events as listeners. Currently, there are 10 possible states
as follows -

```text
  // Database controlled
  ADDED, UPDATED, CANCELLED

  // Scheduler Controlled
  SCHEDULED, EXECUTED

  // Controlled by entities outside the appserver.
  // These will need to be reported to the appserver.
  DELIVERED, OPENED, DISMISSED

  // Miscellaneous
  ERRORED, UNKNOWN
```

REST Endpoints are provided to update and query the STATE. Update can only be made to any of the ones above that can be
updated by external entities(i.e. DELIVERED, OPENED, DISMISSED, ERRORED and UNKNOWN ).

Here is a simple flow between the states --

```text
                                                                                 ┌───────────────────────────────────────────────────────┐
                                                                                 │                                                       │
                                                                                 ▼                                                       │
                                                                        .─────────────────.                                     .─────────────────.
                                                                   _.──'                   `───.                           _.──'                   `───.
                                                                  ╱                             ╲                         ╱                             ╲
                                                          ┌─────▶(           SCHEDULED           ) ─────────────────────▶(            UPDATED            )
                                                          │       `.                           ,────────────┐             `.                           ,'
                                                          │         `───.                 _.──'             │               `───.                 _.──'
                                                          │              `───────────────'                  │                    `───────────────'
              ┌─────────────────────────────────┐         │                      │                          │                            │
              │                                 │         │                      │                          │                            │
              │                                 ▼         │                      │                          │                            │
      ┌───────────────┐                .──────────────────┘                      │                          │                            │
      │               │           _.──'                   `───.                  └────────┐ ┌───────────────│────────────────────────────┘
      │    REST       │          ╱                             ╲                          │ │               │
      │               │────┐    ▲             ADDED             )────┐                    │ │               │
      │               │    │    │`.                           ,'     │                    │ │               │
      └───────────────┘    │    │  `───.                 _.──'       │                    ▼ ▼               │
              ▲            │    │       `───────────────'            │           .─────────────────.        │                   .─────────────────.
    ┌─────────┘            │    │                                    │      _.──'                   `───.   │              _.──'                   `───.
┌───┴─────┐                │    │                                    │     ╱                             ╲  │             ╱                             ╲
│ Request │           ┌────┼────┘                                    ├───▶(            ERRORED            ) └───────────▶(           EXECUTED            )
└─────────┴───┐       │    │                                         │     `.                           ,'                `.                           ,'
              ▼       │    │                                         │       `───.                 ◀─────────┐              `───.                 _.──'
      ┌───────────────┤    │                                         │            `───────────────'          │                   `───────────────'
      │               │    │            .─────────────────.          │                    ▲                  │                           │
      │               │    │       _.──'                   `───.     │                    │                  │                           │
      │     XMPP      │    │      ╱                             ╲    │                    └──────────────────┼───────────────────────────┤
      │               │    └────▶(           CANCELLED           )───┘                                       │                           │
      │               │           `.                           ,'                                            │                           │
      └───────────────┘             `───.                 _.──'                                              │                           │
              │                          `───────────────'                                                   │                           │
              │                                  ▲                                                           │                           ▼
              │                                  │                                                           │                  .─────────────────.
              └──────────────────────────────────┘                                                           │             _.──'                   `───.
                                                                                                             │            ╱                             ╲
                                                                                                             └───────────(           DELIVERED           )
                                                                                                                          `.                           ,'
                                                                                                                            `───.                 _.──'
                                                                                        ┌──────────────────────┐                 `───────────────'
                                        .───────────.                                   │                      │                         │
                                     ,─'             '─.                                ▼                      │                         │
                                   ,'                   `.                       .─────────────.               └─────────────────────────┤
                                  ;                       :                  _.─'               `──.                                ┌────┘
                                  :        UNKNOWN        ;                ,'                       `.                              │
                                   ╲                     ╱                ;         DISMISSED         :                             │
                                    `.                 ,'                 :                           ;                             │
                                      '─.           ,─'                    ╲                         ╱                              ▼
                                         `─────────'                        `.                     ,'                      .─────────────────.
                                                                              `──.             _.─'                   _.──'                   `───.
                                                                                  `───────────'                      ╱                             ╲
                                                                                                                    (            OPENED             )
                                                                                                                     `.                           ,'
                                                                                                                       `───.                 _.──'
                                                                                                                            `───────────────'
```

## Protocols

The AppServer has support for providing Protocols for
the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire) application. Currently, one strategy for
getting the protocols from GitHub (see [RADAR-aRMT-protocols](https://github.com/RADAR-base/RADAR-aRMT-protocols/)) is provided. The AppServer also caches the protocols, so they are still available if there are any issues with GitHub.

|                   Property                    | Description                                                    |              Default              | Required? |
|:---------------------------------------------:|----------------------------------------------------------------|:---------------------------------:|:---------:|
| radar.questionnaire.protocol.github.repo.path | The GitHub repo where protocols are hosted.                    | `RADAR-base/RADAR-aRMT-protocols` |    No     |
| radar.questionnaire.protocol.github.file.name | The filename containing the Protocol for each Project.         |          `protocol.json`          |    No     |
|  radar.questionnaire.protocol.github.branch   | The Branch of the Repository from which to fetch the protocols |             `master`              |    No     |

## Email Notifications

By default, the appserver sends push notifications to mobile devices. Optionally, notifications can be sent via email
in addition. To enable email notifications, configure the `email` section in `appserver.yml`:

1. Set `enabled` to `true`.
2. Set `fromAddress` to the email address from which notifications will be sent.
3. Configure the SMTP server settings.

Example `appserver.yml` configuration:

```yaml
email:
  enabled: true
  smtpHost: smtp.gmail.com
  smtpPort: 587
  smtpUser: my_username
  smtpPassword: my_password
  fromAddress: no-reply@radar.org
  enableTls: true
```

These can also be set via environment variables:

| Environment Variable | Description |
|---|---|
| `RADAR_APPSERVER_NOTIFICATION_EMAIL_ENABLED` | Enable email notifications (`true`/`false`) |
| `RADAR_APPSERVER_NOTIFICATION_EMAIL_FROM` | Sender email address |
| `RADAR_APPSERVER_EMAIL_SMTP_HOST` | SMTP server host |
| `RADAR_APPSERVER_EMAIL_SMTP_PORT` | SMTP server port |
| `RADAR_APPSERVER_EMAIL_SMTP_USERNAME` | SMTP username |
| `RADAR_APPSERVER_EMAIL_SMTP_PASSWORD` | SMTP password |
| `RADAR_APPSERVER_EMAIL_TLS_ENABLED` | Enable TLS (`true`/`false`) |

In addition, in the notification scheduling request set the following fields:

- `emailEnabled` with value `true`.
- (optional) `emailTitle`: subject of the email (notification title will be used when not specified).
- (optional) `emailBody`: body of the email (notification text will be used when not specified).

Example of a request body (partial) to the notification scheduling endpoint:

```json
{
  "emailEnabled": true,
  "emailTitle": "My email title",
  "emailBody": "My email body"
}
```

Note: HTML email is not supported at the moment of this writing.

## Documentation

API docs are available through OpenAPI (Swagger):
- **JSON**: `http://localhost:8080/openapi.json`
- **YAML**: `http://localhost:8080/openapi.yaml`

Each microservice also exposes its own OpenAPI spec at its respective base URL.

## Security

### Management Portal

To enable security via the [RADAR Management Portal](https://github.com/RADAR-base/ManagementPortal), configure the
`auth` section in `appserver.yml`:

```yaml
auth:
  managementPortalUrl: http://localhost:8081/managementportal
  resourceName: res_AppServer
```

This will instantiate all the classes needed for security using the Management Portal. Per-endpoint authorization is
controlled using `@NeedsPermission` annotations on each resource method.

The Management Portal URL and client credentials can also be configured via environment variables:

| Environment Variable | Description |
|---|---|
| `MANAGEMENT_PORTAL_CLIENT_ID` | OAuth client ID |
| `MANAGEMENT_PORTAL_CLIENT_SECRET` | OAuth client secret |

### Management Portal Clients

If security is enabled, make sure the correct resources and scope are set in the OAuth Client
configuration in Management Portal.
The resource `res_AppServer` and scopes `MEASUREMENT.CREATE,SUBJECT.UPDATE,SUBJECT.READ,PROJECT.READ` must be added to
the `aRMT` client. See `appserver-jersey/src/integrationTest/resources/docker/etc/config/oauth_client_details.csv` for
an example.

### Other Security Providers

For using other security providers, configure the security provider in the enhancer factory and modify the authorization annotations on each endpoint method.

## Database Migrations

This project uses [Liquibase](https://www.liquibase.org/) for database schema management. Liquibase tracks which
schema changes have been applied via a `DATABASECHANGELOG` table in the database, so migrations are only run once and
in order.

Changelogs are located at:
- **appserver-jersey**: `appserver-jersey/src/main/resources/db/changelog/changes/`
- **microservices**: each service has its own changelogs under `<service>/src/main/resources/db/changelog/changes/`

A master changelog (`db.changelog-master.yaml`) uses `includeAll` to pick up all changesets in the `changes/` directory,
sorted by filename. Changesets are named with a prefix (`00000000000000_`, `00000000000001_`, etc.) to control ordering.

Liquibase is enabled by default in the `appserver.yml` config:

```yaml
db:
  liquibase:
    enabled: true
    changelogs: db/changelog/db.changelog-master.yaml
  additionalProperties:
    hibernate.hbm2ddl.auto: validate
```

Hibernate runs in `validate` mode — it checks that the schema matches the entity definitions but does not modify the
schema. All schema changes must go through Liquibase changelogs.

For H2 development setups, Liquibase can be disabled and Hibernate can manage the schema directly:

```yaml
db:
  jdbcDriver: org.h2.Driver
  jdbcUrl: jdbc:h2:mem:dev
  hibernateDialect: org.hibernate.dialect.H2Dialect
  liquibase:
    enabled: false
  additionalProperties:
    jakarta.persistence.schema-generation.database.action: drop-and-create
```

### Adding new schema changes

1. Create a new changelog file in the `changes/` directory following the naming convention:
   `00000000000003_update_schema-<yyyyMMddHHmmss>_changelog.yml`
2. Add rollback definitions for reversibility.
3. On startup, Liquibase will automatically detect and apply new changesets.

Migration scripts for moving data between deployment modes (jersey to microservices) are available in `scripts/migration/`.

## Code Quality and Testing

Code quality checks (ktlint) and tests can be run with:

```bash
./gradlew check
```

This will run linting, unit tests and integration tests. Reports are generated in the `build/reports` folder.

### Unit Tests

```bash
# Monolith
./gradlew :appserver-jersey:test

# Microservices
./gradlew :microservices:<service-name>:test
```

### Integration Tests

Integration tests are provided for both the monolith and microservices. They use a running instance of
Management Portal to obtain a valid client token and verify access to resources.

```bash
# Monolith — starts required services via Docker Compose, then runs tests
./gradlew :appserver-jersey:composeUp
./gradlew :appserver-jersey:integrationTest

# Microservices
./gradlew :microservices:integration-tests:composeUp
./gradlew :microservices:integration-tests:integrationTest
```

The integration tests are located at:
- **appserver-jersey**: `appserver-jersey/src/integrationTest/`
- **microservices**: `microservices/integration-tests/src/integrationTest/`

The tests use Management Portal as the security provider. The OAuth helper
(`MpOAuthSupport`) retrieves a valid access token from Management Portal for authenticating test requests.

## Monitoring

### Sentry

To enable Sentry monitoring, set the `SENTRY_DSN` environment variable:

```
SENTRY_DSN: 'https://000000000000.ingest.de.sentry.io/000000000000'
SENTRY_ATTACHSTACKTRACE: true
SENTRY_STACKTRACE_APP_PACKAGES: org.radarbase.appserver
```

## Current Features

- FCM push notification scheduling via Admin SDK
- Data store for user and app data (FCM token mapping, notifications, user metrics)
- Questionnaire protocol management (fetched from GitHub)
- Email notification support
- Database schema management via Liquibase
- OpenAPI/Swagger API documentation
- Sentry monitoring integration
- Management Portal authentication
- Docker and Docker Compose deployment
