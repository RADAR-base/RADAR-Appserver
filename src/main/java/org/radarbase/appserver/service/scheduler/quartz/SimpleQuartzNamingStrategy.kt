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
package org.radarbase.appserver.service.scheduler.quartz

import org.springframework.stereotype.Component

@Component
class SimpleQuartzNamingStrategy : QuartzNamingStrategy {
    override fun getTriggerName(userName: String?, messageId: String?): String {
        return "$TRIGGER_PREFIX$userName-$messageId"
    }

    override fun getJobKeyName(userName: String?, messageId: String?): String {
        return "$JOB_PREFIX$userName-$messageId"
    }

    override fun getMessageId(key: String): String? {
        val keys: Array<String?> = key.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return keys[keys.size - 1]
    }

    companion object {
        private const val TRIGGER_PREFIX = "message-trigger-"
        private const val JOB_PREFIX = "message-jobdetail-"
    }
}
