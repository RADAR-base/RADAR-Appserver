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

import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.entity.UserMetrics
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class UserMapper: Mapper<FcmUserDto, User> {
    override fun dtoToEntity(dto: FcmUserDto): User = User().apply {
        this.fcmToken = dto.fcmToken
        this.subjectId = dto.subjectId
        this.emailAddress = dto.email
        this.usermetrics = getValidUserMetrics(dto)
        this.enrolmentDate = dto.enrolmentDate
        this.timezone = dto.timezone
        this.language = dto.language
        this.attributes = dto.attributes
    }

    override fun entityToDto(entity: User): FcmUserDto = FcmUserDto(entity)

    companion object {
        fun getValidUserMetrics(fcmUserDto: FcmUserDto): UserMetrics {
            val lastOpened: Instant = fcmUserDto.lastOpened ?: Instant.now()
            return UserMetrics(
                lastOpened,
                fcmUserDto.lastDelivered
            )
        }
    }
}