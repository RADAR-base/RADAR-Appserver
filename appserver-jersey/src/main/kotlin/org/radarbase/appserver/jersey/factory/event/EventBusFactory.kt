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

package org.radarbase.appserver.jersey.factory.event

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.event.AppserverEventBus
import org.radarbase.appserver.jersey.event.listener.MessageStateEventListener
import org.radarbase.appserver.jersey.event.listener.TaskStateEventListener

class EventBusFactory @Inject constructor(
    private val taskStateEventListener: TaskStateEventListener,
    private val messageStateEventListener: MessageStateEventListener,
    config: AppserverConfig,
) : DisposableSupplier<EventBus> {
    private val appserverEventBus = AppserverEventBus(
        checkNotNull(config.eventBus.numThreads) {
            "eventBus.numThreads must be set in the configuration file."
        },
    )

    override fun get(): EventBus {
        return appserverEventBus.getEventBus().also {
            it.register(taskStateEventListener)
            it.register(messageStateEventListener)
        }
    }

    override fun dispose(instance: EventBus?) {
        appserverEventBus.close()
    }
}
