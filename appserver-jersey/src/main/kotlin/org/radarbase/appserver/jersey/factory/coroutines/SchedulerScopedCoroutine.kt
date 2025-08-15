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

package org.radarbase.appserver.jersey.factory.coroutines

import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.radarbase.appserver.jersey.config.AppserverConfig

class SchedulerScopedCoroutine @Inject constructor(
    appserverConfig: AppserverConfig,
) : DisposableSupplier<CoroutineScope> {
    private val schedulerConfig = appserverConfig.quartz

    private val dispatcher: CoroutineDispatcher = when (schedulerConfig.coroutineDispatcher) {
        "default" -> Dispatchers.Default
        "io" -> Dispatchers.IO
        else -> Dispatchers.Unconfined
    }

    private val job: Job = when (schedulerConfig.coroutineJob) {
        "supervisor-job" -> SupervisorJob()
        "coroutine-job" -> Job()
        else -> error("Unknown coroutine job for quartz scheduler. Select either 'supervisor-job' or 'coroutine-job'")
    }

    private val scope: CoroutineScope = CoroutineScope(dispatcher + job)

    override fun get(): CoroutineScope = scope

    override fun dispose(instance: CoroutineScope) {
        scope.cancel()
    }
}
