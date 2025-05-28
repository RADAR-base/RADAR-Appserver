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

package org.radarbase.appserver.jersey.mapper

import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.entity.UserMetrics
import java.time.Instant

class UserMapper : Mapper<FcmUserDto, User> {
    override suspend fun dtoToEntity(dto: FcmUserDto): User = User().apply {
        this.fcmToken = dto.fcmToken
        this.subjectId = dto.subjectId
        this.emailAddress = dto.email
        this.usermetrics = getValidUserMetrics(dto)
        this.enrolmentDate = dto.enrolmentDate
        this.timezone = dto.timezone
        this.language = dto.language
        this.attributes = dto.attributes
    }

    override suspend fun entityToDto(entity: User): FcmUserDto = FcmUserDto(entity)

    companion object {
        fun getValidUserMetrics(fcmUserDto: FcmUserDto): UserMetrics {
            val lastOpened: Instant = fcmUserDto.lastOpened ?: Instant.now()
            return UserMetrics(
                lastOpened,
                fcmUserDto.lastDelivered,
            )
        }
    }
}
