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
import java.io.IOException

/**
 * Strategy interface for fetching protocol-related data. Implementations of this
 * interface provide mechanisms to retrieve protocol configurations either globally
 * or per project to be used within the application.
 */
interface ProtocolFetcherStrategy {
    /**
     * Fetches all available protocols mapped to their respective user-id's.
     *
     * @return A map where the keys are user identifiers (String) and the values are their
     * corresponding Protocol objects.
     * @throws IOException If an I/O error occurs while retrieving the protocols.
     */
    @Throws(IOException::class)
    fun fetchProtocols(): Map<String, Protocol>

    /**
     * Fetches protocols associated with each project.
     *
     * @return A map where the keys represent project identifiers as strings and the values
     * represent the corresponding protocols.
     * @throws IOException if there is an issue during the retrieval of the protocols.
     */
    @Throws(IOException::class)
    fun fetchProtocolsPerProject(): Map<String, Protocol>
}
