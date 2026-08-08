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

package org.radarbase.appserver.jersey.event

import com.google.common.eventbus.AsyncEventBus
import io.ktor.utils.io.core.Closeable
import jakarta.inject.Inject
import java.util.concurrent.Executors

class AppserverEventBus @Inject constructor(
    numThreads: Int,
) : Closeable {
    private val executor = Executors.newFixedThreadPool(numThreads)

    fun getEventBus(): AsyncEventBus = AsyncEventBus(
        executor,
    )

    override fun close() {
        executor.shutdown()
    }
}
