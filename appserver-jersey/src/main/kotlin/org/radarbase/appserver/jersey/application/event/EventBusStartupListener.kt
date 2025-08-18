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

package org.radarbase.appserver.jersey.application.event

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import org.radarbase.appserver.jersey.event.listener.MessageStateEventListener
import org.radarbase.appserver.jersey.event.listener.TaskStateEventListener

class EventBusStartupListener @Inject constructor(
    private val eventBus: EventBus,
    private val serviceLocator: ServiceLocator,
) : ApplicationEventListener {

    override fun onEvent(event: ApplicationEvent) {
        if (event.type == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
            val taskListener = serviceLocator.getService(TaskStateEventListener::class.java)
            val messageListener = serviceLocator.getService(MessageStateEventListener::class.java)

            eventBus.register(taskListener)
            eventBus.register(messageListener)
        }
    }

    override fun onRequest(requestEvent: RequestEvent?): RequestEventListener? {
        return null
    }
}
