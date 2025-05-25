package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.DataMessageStateEvent

interface DataMessageStateEventRepository : BaseRepository<DataMessageStateEvent> {
    suspend fun findByDataMessageId(dataMessageId: Long): List<DataMessageStateEvent>
    suspend fun countByDataMessageId(): Long
}
