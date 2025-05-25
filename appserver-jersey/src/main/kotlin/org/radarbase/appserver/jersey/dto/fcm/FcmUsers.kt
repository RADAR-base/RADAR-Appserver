package org.radarbase.appserver.jersey.dto.fcm

import jakarta.validation.constraints.Size

data class FcmUsers(
    @field:Size(max = 1500)
    var users: List<FcmUserDto>,
)
