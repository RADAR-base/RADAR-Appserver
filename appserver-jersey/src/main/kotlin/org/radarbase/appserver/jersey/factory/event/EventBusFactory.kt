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
