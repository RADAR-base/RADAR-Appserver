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

package org.radarbase.appserver.jersey.service.github.protocol

import org.radarbase.appserver.jersey.dto.protocol.Protocol
import java.io.IOException

/**
 * Interface responsible for generating and retrieving protocol configurations.
 */
interface ProtocolGenerator {
    /**
     * Retrieves all available protocols mapped by their identifiers.
     *
     * @return A map where the key is a protocol identifier (as a String) and the value is
     *         the corresponding Protocol object.
     */
    suspend fun retrieveAllProtocols(): Map<String, Protocol>

    /**
     * Retrieves the protocol associated with a specific project ID.
     *
     * @param projectId the identifier of the project whose associated protocol is to be retrieved.
     * @return the protocol corresponding to the given project ID.
     * @throws IOException if an error occurs while retrieving the protocol.
     */
    @Throws(IOException::class)
    suspend fun getProtocol(projectId: String): Protocol

    /**
     * Retrieves the protocol associated with a given subject.
     *
     * @param subjectId The identifier of the subject for which the protocol is to be retrieved.
     * @return The protocol corresponding to the specified subject.
     */
    suspend fun getProtocolForSubject(subjectId: String): Protocol?
}
