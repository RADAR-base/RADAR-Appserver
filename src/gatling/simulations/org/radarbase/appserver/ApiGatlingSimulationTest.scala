package org.radarbase.appserver

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.FiniteDuration

class ApiGatlingSimulationTest extends Simulation {

  val notificationTemplate: String =
    """{
      |    "scheduledTime": 1559702403,
      |    "delivered": false,
      |    "title": "Notif No. - ${np}-${nu}-${nn}-${uuid}",
      |    "body": "Body No. - ${np}-${nu}-${nn}-${uuid}",
      |    "ttlSeconds": 86400,
      |    "sourceId": "z-${np}-${nu}-${nn}-${uuid}",
      |    "fcmMessageId": "-793339996-${np}-${nu}-${nn}-${uuid}",
      |    "type": "ESM",
      |    "appPackage": "aRMT",
      |    "sourceType": "aRMT"
      |}""".replace("|", "")

  val userTemplate: String =
    """{
      |    "subjectId": "test-sub-${np}-${nu}-${uuid}",
      |    "fcmToken" : "shdzdxcv-${np}-${nu}-${uuid}",
      |    "enrolmentDate": "2018-11-29T00:00:00Z",
      |    "timezone": 7200,
      |    "language": "en"
      |  }""".replace("|", "")

  val projectTemplate = """{"projectId": "test-${np}-${uuid}"}"""

  val numOfProjects = 1
  val numOfUsersPerProject = 1
  val numOfNotificationsPerUser = 200
  val numOfSimultaneousClients = 1000

  val baseUrl = "http://localhost:8080"

  val scn: ScenarioBuilder = scenario("Add5ProjectsThen1000UsersThen1000NotificationsPerUserPerProject").repeat(s"$numOfProjects", "np") {
    exec(session => {
      val uuid: UUID = UUID.randomUUID()
      session.set("uuid", uuid)
    })
      .exec(
        http("AddProject-API")
          .post(s"$baseUrl" + "/projects")
          .header("Content-Type", "application/json")
          .body(StringBody(projectTemplate))
          .check(status.is(201))
      ).exitHereIfFailed
      .repeat(s"$numOfUsersPerProject", "nu") {
        exec(
          http("AddUser-API")
            .post(s"$baseUrl" + "/projects/test-${np}-${uuid}/users")
            .header("Content-Type", "application/json")
            .body(StringBody(userTemplate))
            .check(status.is(201))
        ).exitHereIfFailed
          .repeat(s"$numOfNotificationsPerUser", "nn") {
            exec(
              http("AddNotification-API")
                .post(s"$baseUrl" + "/projects/test-${np}-${uuid}/users/test-sub-${np}-${nu}-${uuid}/notifications")
                .header("Content-Type", "application/json")
                .body(StringBody(notificationTemplate))
                .check(status.is(201))
            )
          }
      }
  }

  setUp(scn.inject(atOnceUsers(numOfSimultaneousClients))).maxDuration(FiniteDuration.apply(10, "minutes"))
    .assertions(
      global.responseTime.max.lt(1500),
      forAll.failedRequests.percent.lte(5),
      global.requestsPerSec.gte(20),
      global.responseTime.mean.lte(400)
    )
}