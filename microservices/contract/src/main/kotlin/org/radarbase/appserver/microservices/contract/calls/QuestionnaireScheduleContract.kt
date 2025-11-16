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

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.appendPathSegments
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.dto.protocol.Assessment
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.QUESTIONNAIRE_SCHEDULE
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH
import java.time.Instant

@Suppress("unused")
object QuestionnaireScheduleContract {
    private const val QUESTIONNAIRE_SCHEDULE_SERVICE = "questionnaire-schedule-service"
    private val client = ClientsContract.retrieveClientForService(QUESTIONNAIRE_SCHEDULE_SERVICE)


    suspend fun generateScheduleUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("QuestionnaireScheduleClient::generateScheduleUsingProjectIdAndSubjectId") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, QUESTIONNAIRE_SCHEDULE)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun generateScheduleUsingProtocol(
        assessment: Assessment,
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("QuestionnaireScheduleClient::generateScheduleUsingProtocol") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, QUESTIONNAIRE_SCHEDULE)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun getScheduleUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        type: String,
        search: String,
        startTime: Instant?,
        endTime: Instant?,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("QuestionnaireScheduleClient::generateScheduleUsingProtocol") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, QUESTIONNAIRE_SCHEDULE)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }

    suspend fun deleteScheduleForUser(
        projectId: String,
        subjectId: String,
        type: String,
        search: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("QuestionnaireScheduleClient::generateScheduleUsingProtocol") {
            client.delete(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROJECTS_PATH, projectId, USERS_PATH, subjectId, QUESTIONNAIRE_SCHEDULE)
                }
            }.let {
                createProxyFromResponse(it)
            }
        }
    }
}
