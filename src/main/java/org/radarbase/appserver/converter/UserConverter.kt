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

import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.entity.UserMetrics
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Converter [Converter] class for [User] entity and [FcmUserDto] DTO.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class UserConverter : Converter<User, FcmUserDto> {
    override fun dtoToEntity(fcmUserDto: FcmUserDto): User {
        return User()
            .setFcmToken(fcmUserDto.fcmToken)
            .setSubjectId(fcmUserDto.subjectId)
            .setUserMetrics(getValidUserMetrics(fcmUserDto))
            .setEnrolmentDate(fcmUserDto.enrolmentDate)
            .setTimezone(fcmUserDto.timezone)
            .setLanguage(fcmUserDto.language)
    }

    override fun entityToDto(user: User): FcmUserDto {
        return FcmUserDto(user)
    }

    companion object {
        @JvmStatic
        fun getValidUserMetrics(fcmUserDto: FcmUserDto): UserMetrics {
            val userMetrics: UserMetrics =
                if (fcmUserDto.lastOpened == null && fcmUserDto.lastDelivered == null) {
                    UserMetrics(LocalDateTime.now().toInstant(ZoneOffset.UTC), null)
                } else if (fcmUserDto.lastDelivered == null) {
                    UserMetrics(fcmUserDto.lastOpened, null)
                } else if (fcmUserDto.lastOpened == null) {
                    UserMetrics(
                        LocalDateTime.now().toInstant(ZoneOffset.UTC),
                        fcmUserDto.lastDelivered
                    )
                } else {
                    UserMetrics(fcmUserDto.lastOpened, fcmUserDto.lastDelivered)
                }
            return userMetrics
        }
    }
}