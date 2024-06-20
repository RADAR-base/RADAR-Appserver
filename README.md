# RADAR-Appserver

[![BCH compliance](https://bettercodehub.com/edge/badge/RADAR-base/RADAR-Appserver?branch=master)](https://bettercodehub.com/) [![Build Status](https://travis-ci.org/RADAR-base/RADAR-Appserver.svg?branch=master)](https://travis-ci.org/RADAR-base/RADAR-Appserver) [![Known Vulnerabilities](https://snyk.io//test/github/RADAR-base/RADAR-Appserver/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/RADAR-base/RADAR-Appserver?targetFile=build.gradle)

General purpose application server for the radar platform currently with capability to schedule push notifications.

<!-- TOC -->
* [RADAR-Appserver](#radar-appserver)
  * [Introduction](#introduction)
  * [Getting Started](#getting-started)
  * [REST API](#rest-api)
    * [Quickstart](#quickstart)
  * [FCM](#fcm)
    * [AdminSDK](#adminsdk)
  * [Docker/ Docker Compose](#docker-docker-compose)
  * [Architecture](#architecture)
  * [Notification Lifecycle](#notification-lifecycle)
  * [Protocols](#protocols)
  * [Documentation](#documentation)
  * [Client](#client)
  * [Security](#security)
    * [Management Portal](#management-portal)
    * [Management Portal Clients](#management-portal-clients)
    * [Other Security Providers](#other-security-providers)
  * [Monitoring](#monitoring)
  * [Performance Testing](#performance-testing)
  * [Code-Style and Quality](#code-style-and-quality)
  * [Unit and Integration Testing](#unit-and-integration-testing)
  * [Features](#features)
    * [Feature specific documentation](#feature-specific-documentation)
<!-- TOC -->

## Introduction

This is an app server which provides facilities to store app information (User related) and scheduling of push notifications using Firebase Cloud Messaging. 

This is specifically developed to support the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire) application but can be easily extended or modified to suit the needs of different applications.

The app server provides REST endpoints to interact with the entities and data. For detailed info on the REST API please see the relevant section below.

## Getting Started

1. First you will need to create a Firebase project for your application and add it to your app. This will give you access to all the Firebase services. Follow the instructions on the [official docs](https://firebase.google.com/docs/) according to your platform.  

2. Configure the Server Key and Sender ID (obtained from FCM) in application.properties. 

3. The AppServer needs a database to work. You can either use a `stand-alone` instance of the database of use an in-memory `embedded` instance-
   
   3.1. To use the standalone instance, run the database a docker service by
    
       ```bash
         docker-compose -f src/integrationTest/resources/docker/non_appserver/docker-compose.yml up -d postgres
        ```
        
        This will start the database at `localhost:5432`
        
   3.2. To use as an embedded in-memory database instance (Not recommended for production deployments), set the `spring.datasource.url=jdbc:hsqldb:mem:/appserver` in `application-<profile>.properties`.  Also, change the properties in `src/main/resources/application.properties` to dev or prod according to your requirements. 

4. Build the project using gradle wrapper and run using spring boot. Note: This project uses JAVA 17, please download and install it before building. 

5. The build will need to create a logs directory. The default path is `/usr/local/var/lib/radar/appserver/logs`. Either create the directory there using `sudo mkdir -p /usr/local/var/lib/radar/appserver/logs` followed by `sudo chown $USER /usr/local/var/lib/radar/appserver/logs` or change logs file directory in `src/main/resources/logback-spring.xml` to local log directory like `<property name="LOGS" value="logs" />`

6. The appserver uses the Admin SDK to communicate with the Firebase Cloud Messaging. To 
   configure this, please look at the [FCM section](#fcm).

7. To run the build, run the command below -
   ```bash
    ./gradlew bootRun
   ```
   You can also run in an IDE (like IntelliJ Idea) by giving the `/src/main/java/org/radarbase/appserver/AppserverApplication.java` as the main class.

8. The App-server is now running and is able to send FCM messages. You can make the request to 
   create a project, a user and a notification using the [REST API](#rest-api).

9. Voila!, you will now receive a notification at the schedule time (specified by `scheduledTime` in the payload) on your device.

## REST API

The full API specification and documentation is available via Swagger UI when you launch the app 
server. Please refer to the [Documentation section](#documentation) below.

1. Create a project. If using Management portal, this should be exactly same as the project name 
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
        "scheduledTime": "2022-02-23T09:04:00Z",
        "additionalData": {
        	"questionnaire":"{\"name\":\"ers\",\"questionnaire\":{\"avsc\":\"questionnaire\",\"name\":\"ers\",\"repository\":\"https://raw.githubusercontent.com/RADAR-CNS/RADAR-REDCap-aRMT-Definitions/master/questionnaires/\"},\"protocol\":{\"clinicalProtocol\":null,\"completionWindow\":{\"amount\":1440,\"unit\":\"minutes\"},\"notification\":{\"title\":{\"en\":\"Questionnaire Time\"},\"text\":{\"en\":\"Urgent Questionnaire Pending. Please complete now.\"}},\"reminders\":{\"repeat\":0,\"amount\":0,\"unit\":\"day\"},\"repeatProtocol\":{\"amount\":9999999999,\"unit\":\"minutes\"},\"repeatQuestionnaire\":{\"unitsFromZero\":[0],\"unit\":\"minutes\"}},\"referenceTimestamp\":1645607040.000000000,\"showInCalendar\":true,\"showIntroduction\":false,\"estimatedCompletionTime\":1,\"order\":0,\"isDemo\":false,\"startText\":{},\"endText\":{},\"warn\":{}}",
        	"action":"QUESTIONNAIRE_TRIGGER",
        	"metadata":"\"{}\""
        	
        }
    }
    ```

### Quickstart

The same result as stated in [Getting Started](#getting-started) can be achieved using REST endpoints of the AppServer.

1. Run the AppServer by following the first 3 steps in the [Getting Started](#getting-started) section.

2. Create a new Project by making a `POST` request to the endpoint `http://localhost:8080/projects` with the following body- 
   ```json
      {
      "projectId": "radar"
      }
   ```

3. Create a new User in the Project by making a `POST` request to the endpoint `http://localhost:8080/project/test/users` with the following body-
   ```json
      {
      "subjectId": "sub-1",
      "fcmToken" : "get-this-from-the-device",
      "enrolmentDate": "2019-07-29T00:00:00Z",
      "timezone": 7200,
      "language": "en"
      }
   ```
    **Note:** You will need to get the FCM token from the device and the app. Please see the [setup info](https://firebase.google.com/docs/cloud-messaging) for your platform.
  
4. Add (and schedule) a notification for the above user by making a `POST` request to the endpoint `http://localhost:8080/project/test/users/sub-1/notifications` with the following body-
   ```json
      {
        "title" : "Test Title",
        "body": "Test Body",
        "ttlSeconds": 86400,
        "sourceId": "z",
        "fcmMessageId": "12864132148",
        "type": "ESM",
        "sourceType": "aRMT",
        "appPackage": "aRMT",
        "scheduledTime": "2019-06-29T15:25:58.054Z"
       }
   ```
    Please update the `scheduledTime` to the desired time of notification delivery.

5. You will now receive a notification at the `scheduledTime` for the App and device associated with the FCM token for the user.
  There are other features provided via the REST endpoints. These can be explored using swagger-ui. Please refer to [Documentation](#documentation) section.  
    
## FCM

### AdminSDK
To configure AdminSDK, follow the official Firebase [documentation](https://firebase.google.com/docs/admin/setup#initialize-sdk) till you setup the environment variable (`GOOGLE_APPLICATION_CREDENTIALS`). In the properties 
file, you would need to set `fcmserver.fcmsender` to `org.radarbase.fcm.downstream.AdminSdkFcmSender`. 


## Docker/ Docker Compose
The AppServer is also available as a docker container. Its [Dockerfile](/Dockerfile) is provided with the project. It can be run as follows -

```shell
    docker run -v /logs/:/var/log/radar/appserver/ \
    -v etc/google-credentials.json:/etc/google-credentials.json \
    -e "GOOGLE_APPLICATION_CREDENTIALS=/etc/google-credentials.json" \
    radarbase/radar-appserver:1.1.0
```
Make sure to have the correct path to the google-credentials.json file.

The same can be achieved by running as a docker-compose service. Just specify the following in `docker-compose.yml` file - 

```yml
    services:
      appserver:
        image: radarbase/radar-appserver:1.1.0
        restart: always
        ports:
          - 8080:8080
        volumes:
          - ./radar-is.yml:/resources/radar-is.yml
          - ./logs/:/var/log/radar/appserver/
          - ./etc/google-credentials.json:/etc/google-credentials.json
        environment:
          JDK_JAVA_OPTIONS: -Xmx4G -Djava.security.egd=file:/dev/./urandom
          GOOGLE_APPLICATION_CREDENTIALS: /etc/google-credentials.json
          RADAR_ADMIN_USER: "radar"
          RADAR_ADMIN_PASSWORD: "radar"
          SPRING_APPLICATION_JSON: '{"spring":{"boot":{"admin":{"client":{"url":"http://spring-boot-admin:1111","username":"radar","password":"appserver"}}}}}'
          RADAR_IS_CONFIG_LOCATION: "/resources/radar-is.yml"
          SPRING_BOOT_ADMIN_CLIENT_INSTANCE_NAME: radar-appserver
```

An example `docker-compose` file with all the other components is provided in [integrationTest resources](/src/integrationTest/resources/docker/appserver_dockerhub/docker-compose.yml).

## Architecture
Here is a high level architecture and data flow diagram for the AppServer and its example interaction with a Cordova application (hybrid) like the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire).

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

The Appserver manages the lifecycle of the Notifications through state change events. It uses Pub/Sub paradigm utilising Spring Events so other subscribers can also hook up to the Events as listeners. Currently, there are 10 possible states as follows - 

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
REST Endpoints are provided to update and query the STATE. Update can only be made to any of the ones above that can be updated by external entities(i.e.  DELIVERED, OPENED, DISMISSED, ERRORED and UNKNOWN ).  

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
              │                                  ▲                                                           │                           │
              │                                  │                                                           │                           ▼
              └──────────────────────────────────┘                                                           │                  .─────────────────.
                                                                                                             │             _.──'                   `───.
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
The AppServer has support for providing Protocols for the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire) application. Currently, one strategy for getting the protocols from Github(Take a look at [RADAR-aRMT-protocols](https://github.com/RADAR-base/RADAR-aRMT-protocols/)) is provided. The AppServer also caches the protocols, so they are still available if there are any issues with GitHub. Later, we intend to extend this functionality to add protocols directly in the AppServer possibly by a UI.
You can host your own protocols and configure the following properties - 

|                    Property                   | Description                                                    |              Default              | Required? |
|:---------------------------------------------:|----------------------------------------------------------------|:---------------------------------:|:---------:|
| radar.questionnaire.protocol.github.repo.path | The Github repo where protocols are hosted.                    | `RADAR-base/RADAR-aRMT-protocols` |     No    |
| radar.questionnaire.protocol.github.file.name | The filename containing the Protocol for each Project.         |          `protocol.json`          |     No    |
|   radar.questionnaire.protocol.github.branch  | The Branch of the Repository from which to fetch the protocols |              `master`             |     No    |

## Documentation

Api docs are available through swagger open api 3 config. 
The raw json is present at the `<your-base-url/v3/api-docs>`. By default this should be `http://localhost:8080/v3/api-docs`. This will provide the specification in JSON format. If `YAML` format is preferred, you can query `http://localhost:8080/v3/api-docs.yaml`

The Swagger UI is shown below.
It is present at `<your-base-url/swagger-ui.html`

![swagger UI](/images/swagger-ui.png "Swagger UI Api Docs")

The Swagger API docs are also available at [Swagger Hub](https://app.swaggerhub.com/apis-docs/RADAR-Base/RADAR-Appserver) but may not be most up-to-date. Please check the version matches the app-server that you have deployed.

The Java docs are also available as static content when you build and deploy the app-server. 
These are stored in the `/src/main/resources/static/java-docs` path automatically when building and spring picks this up and exposes it on the path `<your-base-url/java-docs/index.html>` as shown below - 

![java documentation](/images/java-docs.png "Java Docs")

## Client

You can generate a client in 40 different languages for the api using [Swagger Codegen](https://swagger.io/tools/swagger-codegen/) tool. There is even a [javascript library](https://github.com/swagger-api/swagger-codegen#where-is-javascript) that is completely dynamic and does not require static code generation.

## Security

By Default, no OAuth 2.0 security is enabled for the endpoints. Only basic Auth is present on Admin endpoints.
To enable security of specific provider, please read the sections below.

### Management Portal
To enable security via the [RADAR Management Portal](https://github.com/RADAR-base/ManagementPortal), set the following property -
```ini
security.radar.managementportal.enabled=true
security.radar.managementportal.url=<your management portal base url>
```
This will instantiate all the classes needed for security using the management portal. Per endpoint level auth is controlled using Pre and Post annotations for each permission.
All the classes are located in [/src/main/java/org/radarbase/appserver/auth/managementportal](/src/main/java/org/radarbase/appserver/auth/managementportal). 

You can provide the Management Portal specific config in [radar-is.yml](radar-is.yml) file providing the public key endpoint and the resource name. The path to this file should be specified in the env variable `RADAR_IS_CONFIG_LOCATION`.

### Management Portal Clients
If security is enabled, please also make sure that the correct resources and scope are set in the OAuth Client configurations in Management Portal.
The resource `res_AppServer` and scopes `MEASUREMENT.CREATE,SUBJECT.UPDATE,SUBJECT.READ,PROJECT.READ` must be added to the `aRMT` client. Please check the `/src/integrationTest/resources/docker/etc/config/oauth_client_details.csv` file for an example.

### Other Security Providers
For using other type of security providers, set `managementportal.security.enabled=false` and configure the security provider in the spring context and add any necessary classes. See [Management Portal Security](#management-portal) section for an example.

Then you will need to change the `Pre` and `Post` Authorise annotations for each endpoint method according to the semantics provided by your provider. Currently, these are configured to work with Management portal.

## Monitoring

The App server has built in support for the [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready) and can be accessed via the default REST endpoints `<your-base-url>/actuator/*`. To see all the features provided please refer to the [official docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints) of Spring boot actuator.

It also has functionality to register itself as a client to the [Spring Boot Admin Server](http://codecentric.github.io/spring-boot-admin/2.1.1/) for providing a beautiful UI for the Actuator and some other useful admin stuff.
To make this work, 
- Run an instance of the Spring Boot Admin server (various examples on the internet and also via Docker) on the machine and then
 
- configure the client to point to the Admin Server for registration by adding the following to your `application.properties` file - 
  ```ini
    spring.boot.admin.client.url = http://localhost:8888
  ```
  In this case, the Spring Boot admin server was running on `http://localhost:8888`. If http basic auth is enabled on the server also add the following to the `application.properties` file -  
  ```ini
    spring.boot.admin.client.url = http://localhost:8888
    spring.boot.admin.client".username = admin-server-username
    spring.boot.admin.client".password = admin-server-password
  ```

The same can be achieved when deployed with the components as microservices in docker containers using docker-compose. The file [docker-compose.yml](/src/integrationTest/resources/docker/appserver_dockerhub/docker-compose.yml) in this project shows an example of how this is achieved.
Please note how the App server is configured in the container compared to the method of adding properties in `application.properties` file shown above.

Just run - 

```bash
cd src/integrationTest/resources/docker/appserver_dockerhub/
sudo docker-compose up -d
```

Then go to the browser to the URL : `https://localhost:8888` and login with credentials `radar:appserver` to see the Spring Boot Admin Server in action.

Deploying the Spring Boot Admin server and the client as different components makes sure that the same Server can be used to register multiple client apps and the server's lifecycle is not associated with the client. This also means that our client app is lighter and production ready.

## Performance Testing

The app server supports performance testing using Gatling and Scala. The simulations are located in the folder `src/gatling/simulations`.

First run the application and then run the gatling test using the command -

```bash
  ./gradlew gatlingRun
```

To run a particular simulation you can run it like follows - 
```bash
   ./gradlew gatlingRun-org.radarbase.appserver.BatchingApiGatlingSimulationTest
```

You can modify the number of iterations of the requests by change the variables the the top of the file `src/gatling/simulations/org/radarbase/appserver/ApiGatlingSimulationTest.scala` like -
```scala
  val numOfProjects = 5
  val numOfUsersPerProject = 300
  val numOfNotificationsPerUser = 300
  val numOfSimultaneousClients = 2
```

You can also edit the base URL of your deployed instance of the server by changing the value of 
```scala
  val baseUrl = "http://localhost:8080"
``` 

Ideally deploy the server on a remote instance using a persisted database instead of in-memory for real-world testing. Running on your local machine may not reflect the best results.

The reports will be generated at the path `build/reports/gatling/` and the folders will be named `apigatlingsimulationtest-{date-time}`. Open the folder with the correct date time and open the `index.html` file in a browser to view the results. Example result is shown in the screengrab below- 

![gatling test](/images/gatling-results.png "Gatling Test Results")

## Code-Style and Quality

Various tools are enabled to ensure code quality and styling while also doing static code analysis for bugs. PMD, CheckStyle is included and all of these can be run with the command -
```bash
./gradlew check
```
**Note:** This will also run the test and integrationTest.

The reports are generated in the `build/reports` folder. The config files for rules are present in the `config` folder.
A style template following the Google Java style guidelines is also provided for use with IntellJ Idea ([style plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format)) in `config/codestyles` folder.

## Unit and Integration Testing

[Unit Tests](/src/test/java/org/radarbase/appserver) and [Integration Tests](/src/integrationTest/java/org/radarbase/appserver) are provided with the AppServer. These can be run as follows-
```bash
# For Unit tests
./gradlew test
```

```bash
# For Integration Tests
./gradlew integrationTest
```
 
 The integration tests are currently provided for Management Portal as the security provider and uses a running instance of Management Portal to get a valid client token and provide access to resources.
 To change the security provider implement the interface [OAuthHelper](/src/integrationTest/java/org/radarbase/appserver/auth/common/OAuthHelper.java) and provide a valid Access Token in `getAccessToken()` using your security provider.
 Then just use this instance in static Initialisation of the Tests. For more info take a look at [MPOAuthHelper](/src/integrationTest/java/org/radarbase/appserver/auth/common/MPOAuthHelper.java).
 
 You can also run all the checks and tests in a single command using -
```bash
./gradlew check
```
This will run checkstyle, PMD, spot bugs, unit tests and integration tests.

## Features
- Provides a general purpose FCM library with facility to send messages with Admin SDK support.
- Provides functionality of scheduling notifications via FCM.
- Acts as a data store for important user and app related data (like FCM token to subject mapping, notifications, user metrics, etc).
- Can be easily extended for different apps.
- Uses [Liquibase](https://www.liquibase.org/) for easy evolution of database.
- Contains swagger integration for easy API documentation and generation of Java client.
- Uses [lombok.data](https://projectlombok.org/) in most places to reduce boilerplate code and make it more readable.
- Has support for Auditing of database entities.
- Send emails from study subjects (originating from aRMT application) via Firebase _Trigger Email from Firestore_ extension.

### Feature specific documentation
- [Send emails](/docs/send-emails.md)