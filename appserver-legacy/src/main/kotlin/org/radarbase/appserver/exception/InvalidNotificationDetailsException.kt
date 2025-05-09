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

package org.radarbase.appserver.exception

import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serial

/**
 * Exception thrown when a supplied [org.radarbase.appserver.entity.Notification] or
 * [FcmNotificationDto] is invalid. If accessed by REST API then returns HTTP status
 * [HttpStatus.EXPECTATION_FAILED].
 *
 */

@Suppress("unused")
@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class InvalidNotificationDetailsException : RuntimeException {

    companion object {
        @Serial
        private const val serialVersionUID = 793602934508766939L
    }

    constructor(message: String) : super(message)

    constructor(notificationDto: FcmNotificationDto) : super("Invalid details of the Notification supplied : $notificationDto")
}
