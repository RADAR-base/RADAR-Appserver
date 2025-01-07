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
package org.radarbase.appserver.auth.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Suppress("PMD.DataflowAnomalyAnalysis")
class MPOAuthHelper : OAuthHelper {
    companion object {
        private val mapper = ObjectMapper()
        private const val MP_URL = "http://localhost:8081/managementportal/"
        private const val MP_CLIENT = "ManagementPortalapp"
        private const val REST_CLIENT = "pRMT"
        private const val USER = "sub-1"
        private const val ADMIN_USER = "admin"
        private const val ADMIN_PASSWORD = "admin"

        private val MpPairUri = UriComponentsBuilder.fromHttpUrl(MP_URL)
            .pathSegment("api", "oauth-clients", "pair")
            .queryParam("clientId", REST_CLIENT)
            .queryParam("login", USER)
            .toUriString()

        private val MpTokenUri = UriComponentsBuilder.fromHttpUrl(MP_URL)
            .pathSegment("oauth", "token")
            .toUriString()

        private val restTemplate = RestTemplate(HttpComponentsClientHttpRequestFactory()).apply {
            messageConverters.add(MappingJackson2HttpMessageConverter())
        }

        private val ACCESS_TOKEN: String = run {
            val adminAccessToken = obtainAdminAccessToken()
            val tokenUrl = getProperty(makeGetRequest(MpPairUri, adminAccessToken), "tokenUrl")
            val refreshToken = getProperty(makeGetRequest(tokenUrl, adminAccessToken), "refreshToken")
            obtainAccessTokenFromRefreshToken(refreshToken)
        }

        private fun obtainAdminAccessToken(): String {
            val map = LinkedMultiValueMap<String, String>().apply {
                add("username", ADMIN_USER)
                add("password", ADMIN_PASSWORD)
                add("grant_type", "password")
            }

            val headers = HttpHeaders().apply { setBasicAuth(MP_CLIENT, "") }
            val request = HttpEntity(map, headers)

            val response = restTemplate.exchange(MpTokenUri, HttpMethod.POST, request, String::class.java)
            return getProperty(response, "access_token")
        }

        private fun makeGetRequest(url: String, token: String): ResponseEntity<String> {
            val headers = HttpHeaders().apply { setBearerAuth(token) }
            val request = HttpEntity<MultiValueMap<String, String>>(null, headers)
            return restTemplate.exchange(url, HttpMethod.GET, request, String::class.java)
        }

        private fun obtainAccessTokenFromRefreshToken(refreshToken: String): String {
            val map = LinkedMultiValueMap<String, String>().apply {
                add("refresh_token", refreshToken)
                add("grant_type", "refresh_token")
            }

            val headers = HttpHeaders().apply {
                setBasicAuth(REST_CLIENT, "")
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val request = HttpEntity(map, headers)
            val response = restTemplate.exchange(MpTokenUri, HttpMethod.POST, request, String::class.java)
            return getProperty(response, "access_token")
        }

        private fun getProperty(response: ResponseEntity<String>, property: String): String {
            if (response.statusCode.isError) {
                throw IllegalStateException("The request was not successful: ${response.toString()}")
            }
            return try {
                val root: JsonNode = mapper.readTree(response.body)
                root[property]?.asText() ?: throw IllegalStateException("Property not found in the response")
            } catch (e: Exception) {
                throw IllegalStateException("The property $property could not be retrieved from response $response", e)
            }
        }
    }

    override fun getAccessToken(): String = ACCESS_TOKEN
}

