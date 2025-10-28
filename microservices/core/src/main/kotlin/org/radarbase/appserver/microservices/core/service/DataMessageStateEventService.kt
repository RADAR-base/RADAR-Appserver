package org.radarbase.appserver.microservices.core.service

import org.radarbase.appserver.microservices.core.dto.DataMessageStateEventDto
import org.radarbase.appserver.microservices.core.entity.DataMessageStateEvent

interface DataMessageStateEventService {
    suspend fun addDataMessageStateEvent(dataMessageStateEvent: DataMessageStateEvent)

    suspend fun getDataMessageStateEvents(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
    ): List<DataMessageStateEventDto>

    suspend fun getDataMessageStateEventsByDataMessageId(
        dataMessageId: Long,
    ): List<DataMessageStateEventDto>

    suspend fun publishDataMessageStateEventExternal(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
        dataMessageStateEventDto: DataMessageStateEventDto,
    )

}
