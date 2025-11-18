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
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH

@Suppress("unused")
object UserServiceContract {
    private const val USER_SERVICE = "user-service"
    private val client = ClientsContract.retrieveClientForService(USER_SERVICE)

    suspend fun addUser(user: FcmUserDto, projectId: String, forceFcmToken: Boolean, baseUrl: String): ProxyResponse {
        return tryProxyRequest("UserClient::addUser") {
            client.post(normalizedUri(baseUrl)) {
                setBody(user)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH)
                    parameters.append("forceFcmToken", forceFcmToken.toString())
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun updateUser(
        user: FcmUserDto,
        projectId: String,
        subjectId: String,
        forceFcmToken: Boolean,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::updateUser") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId)
                    parameters.append("forceFcmToken", forceFcmToken.toString())
                }
                setBody(user)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getAllUsers(baseUrl: String): ProxyResponse {
        return tryProxyRequest("UserClient::getAllUsers") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(USERS_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getUserUsingId(
        id: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUserUsingId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(USERS_PATH, "user")
                    parameters.append("id", id.toString())
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getUserUsingSubjectId(
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUserUsingSubjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(USERS_PATH, subjectId)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getUserUsingFcmToken(
        fcmToken: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUserUsingFcmToken") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(USERS_PATH, "fcmToken", fcmToken)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }
    suspend fun checkFcmTokenExistsAndReplace(
        subjectId: String,
        userDto: FcmUserDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUserUsingFcmToken") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(USERS_PATH, subjectId, fcmToken)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getUsersUsingProjectId(
        projectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUsersUsingProjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getUserUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::getUserUsingProjectIdAndSubjectId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId)
                }
                accept(ContentType.Application.Json)
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteUserUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("UserClient::deleteUserUsingProjectIdAndSubjectId") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }
}
