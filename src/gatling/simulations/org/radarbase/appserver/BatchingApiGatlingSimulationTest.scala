/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver

import java.time.{Duration, Instant}
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.gatling.commons.validation._
import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.session.Expression
import io.gatling.core.session.el._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import org.radarbase.appserver.dto.fcm.{FcmNotificationDto, FcmNotifications}

import scala.concurrent.duration.FiniteDuration

class BatchingApiGatlingSimulationTest extends Simulation {

  val notifTitleTemplate: Expression[String] = "Notif No. - ${np}-${nu}-${nn}-${uuid}".el[String]
  val notifBodyTemplate: Expression[String] = "Body No. - ${np}-${nu}-${nn}-${uuid}".el[String]
  val notifSourceIdTemplate: Expression[String] = "z-${np}-${nu}-${nn}-${uuid}".el[String]
  val notifMessageIdTemplate: Expression[String] = "-793339996-${np}-${nu}-${nn}-${uuid}".el[String]


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
  val numOfNotificationsPerUser = 100
  val numOfSimultaneousClients = 10

  val baseUrl = "http://localhost:8080"

  val fcmNotifications: FcmNotifications = new FcmNotifications()

  val objectMapper = new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).registerModule(new JavaTimeModule())

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
      )
      .repeat(s"$numOfUsersPerProject", "nu") {
        exec(session => session.set("fcmNotifications", new FcmNotifications()))
          .exec(
            http("AddUser-API")
              .post(s"$baseUrl" + "/projects/test-${np}-${uuid}/users")
              .header("Content-Type", "application/json")
              .body(StringBody(userTemplate))
              .check(status.is(201))
          )
          .repeat(s"$numOfNotificationsPerUser", "nn") {
            exec { session =>
              session("fcmNotifications").validate[FcmNotifications].map(fcmNotifications =>
                fcmNotifications.addNotification(new FcmNotificationDto().setScheduledTime(Instant.now().plus(Duration.ofDays(50)))
                  .setBody(getValue(notifBodyTemplate.apply(session)))
                  .setTitle(getValue(notifTitleTemplate.apply(session)))
                  .setFcmMessageId(getValue(notifMessageIdTemplate.apply(session)))
                  .setSourceId(getValue(notifSourceIdTemplate.apply(session)))
                  .setTtlSeconds(86400)
                  .setAppPackage("aRMT")
                  .setSourceType("aRMT")
                  .setType("ESM")
                  .setDelivered(false)
                )
              )

              def getValue(v: Validation[String]): String = v match {
                case Success(string: String) => string
                case Failure(error: String) => error
              }

              session
            }
          }
          .exec(session =>
            session("fcmNotifications").validate[FcmNotifications].map(f => session.set("body", objectMapper.writeValueAsString(f))).value
          )
          .exec(
            http("AddNotification-API")
              .post(s"$baseUrl" + "/projects/test-${np}-${uuid}/users/test-sub-${np}-${nu}-${uuid}/notifications/batch")
              .header("Content-Type", "application/json")
              .body(StringBody("${body}"))
              .check(status.is(200))
          ).pause("2", TimeUnit.SECONDS)
      }
  }

  setUp(scn.inject(atOnceUsers(numOfSimultaneousClients))).maxDuration(FiniteDuration.apply(10, "minutes"))
    .assertions(
      global.responseTime.max.lt(5000),
      forAll.failedRequests.percent.lte(3),
      global.requestsPerSec.gte(15),
      global.responseTime.mean.lte(400)
    )
}
