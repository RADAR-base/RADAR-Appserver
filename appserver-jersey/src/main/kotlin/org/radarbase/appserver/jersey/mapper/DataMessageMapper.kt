package org.radarbase.appserver.jersey.mapper

import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.jersey.entity.DataMessage

class DataMessageMapper : Mapper<FcmDataMessageDto, DataMessage> {
    override suspend fun dtoToEntity(dto: FcmDataMessageDto): DataMessage {
        return DataMessage.DataMessageBuilder().apply {
            mutableContent(dto.mutableContent)
            priority(dto.priority)
            fcmCondition(dto.fcmCondition)
            fcmTopic(dto.fcmTopic)
            fcmMessageId(dto.hashCode().toString())
            appPackage(dto.appPackage)
            sourceType(dto.sourceType)
            sourceId(dto.sourceId)
            ttlSeconds(dto.ttlSeconds)
            scheduledTime(dto.scheduledTime)
            dataMap(dto.dataMap)
        }.build()
    }

    override suspend fun entityToDto(entity: DataMessage): FcmDataMessageDto {
        return FcmDataMessageDto(entity)
    }
}
