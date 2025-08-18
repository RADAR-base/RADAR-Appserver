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

package org.radarbase.appserver.service.questionnaire.protocol

import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.util.CachedMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.Duration

/**
 * Default implementation of the ProtocolGenerator interface, responsible for
 * retrieving and caching protocol configurations. This class interacts with a
 * ProtocolFetcherStrategy to fetch protocol-related data and utilizes caching
 * techniques to minimize redundant network calls.
 *
 * @param protocolFetcherStrategy A [ProtocolFetcherStrategy] interface implementation used for
 * retrieving protocol configurations.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class DefaultProtocolGenerator(
    @field:Transient private val protocolFetcherStrategy: ProtocolFetcherStrategy,
) : ProtocolGenerator {
    /**
     * Caches a mapping of user identifiers to their corresponding protocol configurations.
     */
    @Transient
    private var cachedProtocolMap: CachedMap<String, Protocol> = CachedMap<String, Protocol>(
        protocolFetcherStrategy::fetchProtocols,
        CACHE_INVALIDATE_DEFAULT,
        CACHE_RETRY_DEFAULT,
    )

    /**
     * Caches a mapping of project identifiers to their corresponding protocol configurations.
     */
    @Transient
    private var cachedProjectProtocolMap: CachedMap<String, Protocol> = CachedMap<String, Protocol>(
        protocolFetcherStrategy::fetchProtocolsPerProject,
        CACHE_INVALIDATE_DEFAULT,
        CACHE_RETRY_DEFAULT,
    )

    /**
     * Retrieves all available protocols from the cache or previously fetched data.
     * If an error occurs during protocol retrieval, cached values are used as a fallback.
     *
     * @return A map where the key is a protocol identifier (as a String) and the value is
     *         the corresponding Protocol object.
     */
    override fun retrieveAllProtocols(): Map<String, Protocol> {
        return try {
            cachedProtocolMap.get()
        } catch (ex: IOException) {
            logger.warn("Cannot retrieve Protocols, using cached values if available.", ex)
            cachedProtocolMap.getCachedMap()
        }
    }

    /**
     * Retrieves the protocol associated with the specified project ID.
     *
     * @param projectId the identifier of the project whose associated protocol is to be retrieved.
     * @return the protocol corresponding to the given project ID.
     * @throws IOException if an error occurs while retrieving the protocol or if the protocol cannot be fetched.
     */
    @Throws(IOException::class)
    override fun getProtocol(projectId: String): Protocol {
        try {
            return cachedProjectProtocolMap[projectId]!!
        } catch (ex: IOException) {
            logger.warn(
                "Cannot retrieve Protocols for project {} : {}, Using cached values.",
                projectId,
                ex.toString(),
            )
            return cachedProjectProtocolMap.getCachedMap()[projectId]!!
        } catch (ex: Exception) {
            logger.warn(
                "Exception while fetching protocols for project {}",
                projectId,
                ex,
            )
            throw IOException("Exception while fetching protocols for project $projectId", ex)
        }
    }

    /**
     * Retrieves the protocol associated with the given subject ID.
     *
     * @param subjectId The identifier of the subject for which the protocol is being retrieved.
     * @return The corresponding Protocol object for the specified subject ID.
     * @throws IOException If an error occurs while retrieving protocols or handling cached values.
     */
    @Throws(IOException::class)
    private fun forceGetProtocolForSubject(subjectId: String): Protocol? {
        try {
            return cachedProtocolMap.get(true)[subjectId]
        } catch (ex: IOException) {
            logger.warn("Cannot retrieve Protocols, using cached values if available.", ex)
            return cachedProtocolMap.getCachedMap()[subjectId]!!
        } catch (ex: Exception) {
            logger.warn(
                "Exception while fetching protocols for subject {}",
                subjectId,
                ex,
            )
            throw IOException("Exception while fetching protocols for subject $subjectId", ex)
        }
    }

    /**
     * Retrieves the protocol associated with the specified subject. If the protocol is
     * not available in the cache, it attempts to fetch it using a fallback strategy.
     *
     * @param subjectId The identifier of the subject for which the protocol is being retrieved.
     * @return The protocol corresponding to the given subject.
     * @throws IOException If an error occurs during protocol retrieval and no cached value is available.
     */
    override fun getProtocolForSubject(subjectId: String): Protocol? {
        try {
            val protocol = cachedProtocolMap[subjectId]
            if (protocol == null) {
                return forceGetProtocolForSubject(subjectId)
            }
            return protocol
        } catch (ex: IOException) {
            logger.warn(
                "Cannot retrieve Protocols for subject {} : {}, Using cached values.",
                subjectId,
                ex.toString(),
            )
            return cachedProtocolMap.getCachedMap()[subjectId]!!
        } catch (_: NoSuchElementException) {
            logger.warn("Subject does not exist in map. Fetching..")
            return forceGetProtocolForSubject(subjectId)
        } catch (ex: Exception) {
            logger.warn(
                "Exception while fetching protocols for subject {}",
                subjectId,
                ex,
            )
            throw IOException("Exception while fetching protocols for subject $subjectId", ex)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DefaultProtocolGenerator::class.java)

        /**
         * Represents the default duration after which cached protocol data is considered invalid
         * and must be refreshed.
         */
        private val CACHE_INVALIDATE_DEFAULT: Duration = Duration.ofHours(1)

        /**
         * The default duration to retry caching operations.
         * Used when no specific retry interval is configured.
         */
        private val CACHE_RETRY_DEFAULT: Duration = Duration.ofHours(2)
    }
}
