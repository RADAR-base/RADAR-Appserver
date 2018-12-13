General purpose application server for the radar platform currently with capability to schedule push notifications.

# Introduction

This is an app server which provides facilities to store app information (User related) and scheduling of push notifications using Firebase Cloud Messaging. 

This is specifically developed to support the [RADAR-Questionnaire](https://github.com/RADAR-base/RADAR-Questionnaire) application but can be easily extended or modified to suit the needs of different applications.

The app server provides REST endpoints to interact with the entities and data. For detailed info on the REST API please see the relevant section below.
There is also support for legacy XMPP protocol for FCM.


# Getting Started

1. First you will need to create a Firebase project for your application and add it to your app. This will give you access to all the Firebase services. Follow the instructions on the [official docs](https://firebase.google.com/docs/) according to your platform.  

2. Configure the Server Key and Sender ID (obtained from FCM) in application.properties. 

3. Build the project using gradle wrapper and run using spring boot. Note: This project uses JAVA 11, please download and install it before building. On mac or linux, run the below -
    ```bash
    ./gradlew bootRun
    ```
    You can also run in an IDE (like IntelliJ Idea) by giving the `/RADAR-Appserver/src/main/java/org/radarbase/appserver/AppserverApplication.java` as the main class.
    
4. The App-server is now connected to the FCM XMPP server and is able to send and receive messages. On your mobile application, try sending an upstream message using the FCM sdk for your platform. Notification scheduling parses payloads from upstream messages containing the action SCHEDULE. The format of the data payload of upstream message should contain at least-    
    ```javascript
    {
     "data":
         {
             "notificationTitle":"Schedule id 1",
             "notificationMessage":"hello",
             "action":"SCHEDULE",
             "time":"1531482349791",
             "subjectId":"test",
             "projectID":"test",
             "ttlSeconds":"900"
          },
          ...
     }
    ```
5. Voila!, you will now receive a notification at the schedule time (specified by `time` in the payload) on your device.

6. You can also achieve the same using more reliable and flexible REST API using the schedule endpoint. Please refer to REST API section below for more info.

# REST API
// TODO 

# FCM
// TODO

# Current Features
- Provides a general purpose FCM library with facility to send and receive messages using XMPP protocol. Admin SDK support to be added later.
- Can configure which type of FCM sender to use via properties (so can be changed dynamically if required).
- Provides functionality of scheduling notifications via FCM.
- Acts as a data store for important user and app related data (like FCM token to subject mapping, notifications, user metrics, etc).
- Can be easily extended for different apps.
- Uses [Liquibase](https://www.liquibase.org/) for easy evolution of database.
- Contains swagger integration for easy API documentation and generation of Java client.
- Uses [lombok.data](https://projectlombok.org/) in most places to reduce boiler plate code and make it more readable.
- Has support for Auditing of database entities.
- Uses and extends the Spring XMPP integration library for implementing the XMPP protocol. 

// WIP

# TODO

- Add better documentation.
- Add validation of notification requests using the protocol and enrolment date of the user.
- Add endpoint to filter notifications based on different params.
- Update lastOpened metric for user when a request is received.
- Add batch scheduling of notifications.
- Add Management Portal service for getting an missing info if not present in a request.
- Add support for sending messages via Firebase Admin SDK.
- Make the Xmpp Connection more robust by adding reconnection logic.
- Break out the org.radarbase.fcm package into its own module to be used as a library.
- Add docker builds.
- Add a Angular UI to view, update and schedule/send notifications to subjects.
- Investigate the feasibility of adding as an integration to the Management Portal for various tasks. (For instance, a new token can be sent via push notification using a button on MP to the device if the token has expired).

// WIP