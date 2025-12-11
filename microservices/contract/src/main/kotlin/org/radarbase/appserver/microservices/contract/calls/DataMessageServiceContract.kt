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
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessages
import org.radarbase.appserver.microservices.core.utils.Paths.ALL_KEYWORD
import org.radarbase.appserver.microservices.core.utils.Paths.MESSAGING_DATA_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH

object DataMessageServiceContract {
    private const val DATA_MESSAGE_SERVICE = "data-message-service"
    private val client = ClientsContract.retrieveClientForService(DATA_MESSAGE_SERVICE)

    suspend fun getAllDataMessages(
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::getAllDataMessages") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_DATA_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getDataMessageUsingId(
        id: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::getDataMessageUsingId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_DATA_PATH, id.toString())
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getFilteredDataMessages(
        type: String?,
        delivered: Boolean?,
        ttlSeconds: Int?,
        startTimeStr: String?,
        endTimeStr: String?,
        limit: Int?,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::getFilteredDataMessages") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(MESSAGING_DATA_PATH, "filtered")

                    type?.let { parameters.append("type", it) }
                    delivered?.let { parameters.append("delivered", it.toString()) }
                    ttlSeconds?.let { parameters.append("ttlSeconds", it.toString()) }
                    startTimeStr?.let { parameters.append("startTimeStr", it) }
                    endTimeStr?.let { parameters.append("endTimeStr", it) }
                    limit?.let { parameters.append("limit", it.toString()) }
                }

                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getDataMessageUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::getDataMessageUsingProjectIdAndSubjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_DATA_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getDataMessagesUsingProjectId(
        projectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::getDataMessageUsingProjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, MESSAGING_DATA_PATH)
                }

                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun addSingleDataMessage(
        projectId: String,
        subjectId: String,
        fcmDataMessage: FcmDataMessageDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::addSingleDataMessage") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_DATA_PATH)
                }
                setBody(fcmDataMessage)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun addBatchDataMessages(
        projectId: String,
        subjectId: String,
        fcmDataMessages: FcmDataMessages,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::addBatchDataMessage") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_DATA_PATH, "batch")
                }
                setBody(fcmDataMessages)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun updateDataMessage(
        projectId: String,
        subjectId: String,
        dataMessageDto: FcmDataMessageDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::updateDataMessage") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_DATA_PATH)
                }
                setBody(dataMessageDto)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteDataMessageForUser(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::deleteDataMessageForUser") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH,
                        projectId,
                        USERS_PATH,
                        subjectId,
                        MESSAGING_DATA_PATH,
                        ALL_KEYWORD,
                    )
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteDataMessageUsingProjectIdAndSubjectIdAndNotificationId(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("DataMessageClient::deleteDataMessageUsingProjectIdAndSubjectIdAndNotificationId") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(
                        PROJECTS_PATH, projectId, USERS_PATH, subjectId, MESSAGING_DATA_PATH, dataMessageId.toString(),
                    )
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }
}
