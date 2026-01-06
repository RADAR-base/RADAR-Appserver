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

package org.radarbase.appserver.microservices.cloud.messaging.config

import org.radarbase.appserver.microservices.core.config.CoreEventBusConfig
import org.radarbase.appserver.microservices.core.config.CoreFcmServerConfig
import org.radarbase.appserver.microservices.core.config.CoreSchedulerConfig
import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.jersey.config.ConfigLoader.copyOnChange
import org.radarbase.jersey.enhancer.EnhancerFactory

data class CloudMessagingServiceConfig(
    val resourceConfig: Class<out EnhancerFactory>,
    val server: CloudMessagingServerConfig,
    val db: CloudMessagingDbConfig,
    val contract: ContractConfig,
    val eventBus: CoreEventBusConfig,
    val fcm: CoreFcmServerConfig,
    val quartz: CoreSchedulerConfig = CoreSchedulerConfig(),
    val email: EmailConfig = EmailConfig(),
) : Validation {
    override fun validate() {
        listOf(server, db, eventBus).forEach {
            it.validate()
        }
    }

    fun withEnv() = this.copyOnChange(
        db,
        {
            it.withEnv()
        },
        {
            copy(db = it)
        },
    ).copyOnChange(
        server,
        {
            it.withEnv()
        },
        {
            copy(server = it)
        },
    ).copyOnChange(
        contract,
        {
            it.withEnv()
        },
        {
            copy(contract = it)
        },
    ).copyOnChange(
        email,
        {
            it.withEnv()
        }, {
            copy(email = it)
        }
    )
}
