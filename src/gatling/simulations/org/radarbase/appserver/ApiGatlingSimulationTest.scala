package org.radarbase.appserver

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.scenario.{Scenario, Simulation}
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.FiniteDuration

class ApiGatlingSimulationTest extends Simulation {

  val notificationTemplate: String =
    """{
      |    "scheduledTime": 1559702403,
      |    "delivered": false,
      |    "title": "Notif No. - ${np}-${nu}-${nn}",
      |    "body": "Body No. - ${np}-${nu}-${nn}",
      |    "ttlSeconds": 86400,
      |    "sourceId": "z-${np}-${nu}-${nn}",
      |    "fcmMessageId": "-793339996",
      |    "type": "ESM",
      |    "appPackage": "aRMT",
      |    "sourceType": "aRMT"
      |}""".replace("|", "")

  val userTemplate: String =
    """{
      |    "subjectId": "test-sub-${np}-${nu}",
      |    "fcmToken" : "shdzdxcv-${np}-${nu}",
      |    "enrolmentDate": "2018-11-29T00:00:00Z",
      |    "timezone": 7200,
      |    "language": "en"
      |  }""".replace("|", "")

  val projectTemplate = """{"projectId": "test-${np}"}"""

  val numOfProjects = 2
  val numOfUsersPerProject = 80
  val numOfNotificationsPerUser = 80

  val baseUrl = "http://localhost:8080"

  val scn: ScenarioBuilder = scenario("Add5ProjectsThen1000UsersThen1000NotificationsPerUserPerProject").repeat(s"$numOfProjects", "np") {
    exec(
      http("AddProject-API")
        .post(s"$baseUrl" + "/projects")
        .header("Content-Type", "application/json")
        .body(StringBody(projectTemplate))
        .check(status.is(201))
    )
      .repeat(s"$numOfUsersPerProject", "nu") {
        exec(
          http("AddUser-API")
            .post(s"$baseUrl" + "/projects/test-${np}/users")
            .header("Content-Type", "application/json")
            .body(StringBody(userTemplate))
            .check(status.is(201))
        )
          .repeat(s"$numOfNotificationsPerUser", "nn") {
            exec(
              http("AddNotification-API")
                .post(s"$baseUrl" + "/projects/test-${np}/users/test-sub-${np}-${nu}/notifications")
                .header("Content-Type", "application/json")
                .body(StringBody(notificationTemplate))
                .check(status.is(201))
            )
          }
      }
  }

  // The above is performed with 30 users simultaneously
  setUp(scn.inject(atOnceUsers(1))).maxDuration(FiniteDuration.apply(10, "minutes"))
}