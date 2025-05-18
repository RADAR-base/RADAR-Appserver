package org.radarbase.appserver.jersey.service.github.protocol

import io.ktor.utils.io.errors.IOException
import org.radarbase.appserver.jersey.dto.protocol.Protocol

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
    suspend fun fetchProtocols(): Map<String, Protocol>

    /**
     * Fetches protocols associated with each project.
     *
     * @return A map where the keys represent project identifiers as strings and the values
     * represent the corresponding protocols.
     * @throws IOException if there is an issue during the retrieval of the protocols.
     */
    @Throws(IOException::class)
    suspend fun fetchProtocolsPerProject(): Map<String, Protocol>
}
