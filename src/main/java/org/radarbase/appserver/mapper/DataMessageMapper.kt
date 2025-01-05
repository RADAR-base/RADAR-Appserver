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
package org.radarbase.appserver.mapper

import org.radarbase.appserver.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.DataMessage.DataMessageBuilder
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Converter [Mapper] class for [DataMessage] entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class DataMessageMapper : Mapper<FcmDataMessageDto, DataMessage> {
    override fun dtoToEntity(dto: FcmDataMessageDto): DataMessage {
        return DataMessageBuilder().apply {
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

    override fun entityToDto(dataMessage: DataMessage): FcmDataMessageDto {
        return FcmDataMessageDto(dataMessage)
    }
}
