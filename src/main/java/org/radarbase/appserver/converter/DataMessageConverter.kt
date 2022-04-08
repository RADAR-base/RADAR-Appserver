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
package org.radarbase.appserver.converter

import org.radarbase.appserver.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.entity.DataMessage
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Converter [Converter] class for [DataMessage] entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class DataMessageConverter : Converter<DataMessage, FcmDataMessageDto> {

    override fun dtoToEntity(dataMessageDto: FcmDataMessageDto): DataMessage {
        return DataMessage(
            mutableContent = dataMessageDto.isMutableContent,
            priority = dataMessageDto.priority,
            fcmCondition = dataMessageDto.fcmCondition,
            fcmTopic = dataMessageDto.fcmTopic,
            fcmMessageId = dataMessageDto.hashCode().toString(),
            appPackage = dataMessageDto.appPackage,
            sourceType = dataMessageDto.sourceType,
            sourceId = dataMessageDto.sourceId,
            ttlSeconds = dataMessageDto.ttlSeconds,
            scheduledTime = dataMessageDto.scheduledTime,
            dataMap = dataMessageDto.dataMap
        )
    }

    override fun entityToDto(dataMessage: DataMessage): FcmDataMessageDto {
        return FcmDataMessageDto(dataMessage)
    }
}