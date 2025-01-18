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
package org.radarbase.appserver.controller

object PathsUtil {
    const val USER_PATH: String = "users"
    const val PROJECT_PATH: String = "projects"
    const val TOPIC_PATH: String = "topics"
    const val FILE_PATH: String = "files"
    const val MESSAGING_NOTIFICATION_PATH: String = "messaging/notifications"
    const val MESSAGING_DATA_PATH: String = "messaging/data"
    const val PROTOCOL_PATH: String = "protocols"
    const val PROJECT_ID_CONSTANT: String = "{projectId}"
    const val SUBJECT_ID_CONSTANT: String = "{subjectId}"
    const val TOPIC_ID_CONSTANT: String = "{topicId}"
    const val NOTIFICATION_ID_CONSTANT: String = "{notificationId}"
    const val NOTIFICATION_STATE_EVENTS_PATH: String = "state_events"
    const val QUESTIONNAIRE_SCHEDULE_PATH: String = "questionnaire/schedule"
    const val QUESTIONNAIRE_TRIGGER_PATH: String = "questionnaire/trigger"
    const val QUESTIONNAIRE_STATE_EVENTS_PATH: String = "state_events"
    const val TASK_PATH: String = "tasks"
    const val TASK_ID_CONSTANT: String = "{taskId}"
    const val ALL_KEYWORD: String = "all"
    const val GITHUB_PATH: String = "github"
    const val GITHUB_CONTENT_PATH: String = "content"
}
