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
