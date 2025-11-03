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

package org.radarbase.appserver.microservices.contract.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds


object ClientsContract {
    private val clients: ConcurrentHashMap<String, HttpClient> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(ClientsContract::class.java)

    fun retrieveClientForService(
        service: String,
        timeoutMs: Long? = null,
        responseValidation: Boolean = true,
    ): HttpClient {
        return clients.computeIfAbsent(service) {
            HttpClient(CIO) {
                expectSuccess = responseValidation
                install(HttpTimeout) {
                    val t = timeoutMs ?: 15.seconds.inWholeMilliseconds
                    connectTimeoutMillis = t
                    socketTimeoutMillis = t
                    requestTimeoutMillis = t
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        }
                    )
                }
            }
        }
    }

    suspend fun closeClientConnections() = coroutineScope {
        val snapshot = clients.entries.toList()
        val jobs = snapshot.map { (_, client) ->
            async(Dispatchers.IO) {
                try {
                    client.close()
                } catch (e: Exception) {
                    logger.warn("Failed to close client", e)
                }
            }
        }
        jobs.awaitAll()
        snapshot.forEach { (service, client) ->
            clients.remove(service, client)
        }
    }

    fun closeServiceClient(service: String) {
        clients.remove(service)?.let { client ->
            try {
                client.close()
            } catch (e: Exception) {
                logger.warn("Failed to close client for $service", e)
            }
            logger.info("Closed contract client for service ($service)")
        } ?: logger.warn("No contract client to close for service ($service)")
    }
}
