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

package org.radarbase.appserver.microservices.user.config

import org.radarbase.appserver.microservices.contract.utils.Env.PROJECT_SERVICE_BASE_URL
import org.radarbase.appserver.microservices.contract.utils.Env.TASK_SERVICE_BASE_URL
import org.radarbase.jersey.config.ConfigLoader.copyEnv

data class ContractConfig(
    val project: String,
    val task: String
) {
    fun withEnv(): ContractConfig = this
        .copyEnv(PROJECT_SERVICE_BASE_URL) {
            copy(project = it)
        }
        .copyEnv(TASK_SERVICE_BASE_URL) {
            copy(task = it)
        }
}
