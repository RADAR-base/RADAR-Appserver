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
