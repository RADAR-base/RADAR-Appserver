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
