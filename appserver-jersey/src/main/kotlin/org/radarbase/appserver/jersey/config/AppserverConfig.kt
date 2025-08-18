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

package org.radarbase.appserver.jersey.config

import org.radarbase.appserver.jersey.config.github.GithubConfig
import org.radarbase.appserver.jersey.config.questionnaire.QuestionnaireProtocolConfig
import org.radarbase.jersey.enhancer.EnhancerFactory

data class AppserverConfig(
    val resourceConfig: Class<out EnhancerFactory>,
    val server: ServerConfig,
    val auth: AuthConfig = AuthConfig(),
    val fcm: FcmServerConfig = FcmServerConfig(),
    val github: GithubConfig = GithubConfig(),
    val quartz: SchedulerConfig = SchedulerConfig(),
    val db: DbConfig = DbConfig(),
    val email: EmailConfig = EmailConfig(),
    val eventBus: EventBusConfig = EventBusConfig(),
    val protocol: QuestionnaireProtocolConfig = QuestionnaireProtocolConfig(),
) : Validation {
    override fun validate() {
        listOf(auth, server, db).forEach { validation: Validation ->
            validation.validate()
        }
    }
}
