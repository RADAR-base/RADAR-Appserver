/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.contract.calls

import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.dto.NotificationStateEventDto
import org.radarbase.appserver.microservices.core.utils.Paths.MESSAGING_NOTIFICATION_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.NOTIFICATION_STATE_EVENTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH

object NotificationStateEventServiceContract {
    private const val NOTIFICATION_STATE_EVENT_SERVICE = "notification-state-event-service"
    private val client = ClientsContract.retrieveClientForService(NOTIFICATION_STATE_EVENT_SERVICE)

    suspend fun getNotificationStateEventsByNotificationId(notificationId: Long, baseUrl: String): ProxyResponse {
        return tryProxyRequest("NotificationStateEventClient::getNotificationStateEventsByNotificationId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        MESSAGING_NOTIFICATION_PATH,
                        notificationId.toString(),
                        NOTIFICATION_STATE_EVENTS_PATH,
                    )
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getNotificationStateEvents(
        projectId: String,
        subjectId: String,
        notificationId: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationStateEventService::getNotificationStateEvents") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        notificationId.toString(),
                        NOTIFICATION_STATE_EVENTS_PATH,
                    )
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun postNotificationStateEvents(
        projectId: String,
        subjectId: String,
        notificationId: Long,
        notificationStateEventDto: NotificationStateEventDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationStateEventClient::postNotificationStateEvents") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        notificationId.toString(),
                        NOTIFICATION_STATE_EVENTS_PATH,
                    )
                }
                setBody(notificationStateEventDto)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }
}
