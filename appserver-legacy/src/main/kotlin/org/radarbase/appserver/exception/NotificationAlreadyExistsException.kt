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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import java.io.Serial

/**
 * Exception thrown when a requested [org.radarbase.appserver.entity.Notification] that needs
 * to be added/created already exists.
 */
@Suppress("unused")
@JsonIgnoreProperties(value = ["cause", "stackTrace", "suppressed", "localizedMessage"])
class NotificationAlreadyExistsException : RuntimeException {

    companion object {
        @JsonIgnore
        @Serial
        private val serialVersionUID: Long = -79364859476939L
    }

    var errorMessage: String? = null
        private set
    var dto: FcmNotificationDto? = null
        private set

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, `object`: FcmNotificationDto) : super("$message $`object`") {
        this.dto = `object`
        this.errorMessage = message
    }
}
