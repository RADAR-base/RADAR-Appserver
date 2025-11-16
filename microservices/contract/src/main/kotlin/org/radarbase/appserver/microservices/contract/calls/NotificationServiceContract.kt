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

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotifications
import org.radarbase.appserver.microservices.core.utils.Paths.ALL_KEYWORD
import org.radarbase.appserver.microservices.core.utils.Paths.MESSAGING_NOTIFICATION_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.TASKS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH

object NotificationServiceContract {
    private const val NOTIFICATION_SERVICE = "notification-service"
    private val client: HttpClient = ClientsContract.retrieveClientForService(NOTIFICATION_SERVICE)

    suspend fun getAllNotifications(
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::getAllNotifications") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_NOTIFICATION_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getNotificationUsingId(
        id: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::getNotificationUsingId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_NOTIFICATION_PATH, id.toString())
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getFilteredNotifications(
        type: String?,
        delivered: Boolean?,
        ttlSeconds: Int?,
        startTimeStr: String?,
        endTimeStr: String?,
        limit: Int?,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::getFilteredNotifications") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_NOTIFICATION_PATH, "filter")

                    type?.let { parameters.append("type", it) }
                    delivered?.let { parameters.append("delivered", it.toString()) }
                    ttlSeconds?.let { parameters.append("ttlSeconds", it.toString()) }
                    startTimeStr?.let { parameters.append("startTime", it) }
                    endTimeStr?.let { parameters.append("endTime", it) }
                    limit?.let { parameters.append("limit", it.toString()) }
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getNotificationsUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::getNotificationsUsingProjectIdAndSubjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_NOTIFICATION_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getNotificationsUsingProjectId(
        projectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::getNotificationsUsingProjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, MESSAGING_NOTIFICATION_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun addSingleNotification(
        projectId: String,
        subjectId: String,
        notification: FcmNotificationDto,
        schedule: Boolean,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::addSingleNotification") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_NOTIFICATION_PATH)
                    parameters.append("schedule", schedule.toString())
                }
                setBody(notification)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun scheduleUserNotifications(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::scheduleUserNotifications") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        "schedule",
                    )
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun scheduleUserNotification(
        projectId: String,
        subjectId: String,
        notificationId: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::scheduleUserNotification") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        notificationId.toString(),
                        "schedule",
                    )
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun addBatchNotifications(
        projectId: String,
        subjectId: String,
        schedule: Boolean,
        notifications: FcmNotifications,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::scheduleUserNotification") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        "batch",
                    )

                    parameters.append("schedule", schedule.toString())
                }
                setBody(notifications)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun updateNotification(
        projectId: String,
        subjectId: String,
        notification: FcmNotificationDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::updateNotification") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                    )
                }
                setBody(notification)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteNotificationsForUser(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::deleteNotificationsForUser") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        ALL_KEYWORD
                    )
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
        projectId: String,
        subjectId: String,
        id: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::deleteNotificationsForUser") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        id.toString()
                    )
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteNotificationUsingProjectIdAndSubjectIdAndTaskId(
        projectId: String,
        subjectId: String,
        taskId: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("NotificationServiceClient::deleteNotificationsForUser") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_NOTIFICATION_PATH,
                        TASKS_PATH,
                        taskId.toString()
                    )
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

}
