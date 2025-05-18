package org.radarbase.appserver.jersey.service.scheduling.factory

import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.radarbase.appserver.jersey.service.scheduling.SchedulingService

class SchedulingServiceFactory : DisposableSupplier<SchedulingService> {
    override fun get() = SchedulingService()

    override fun dispose(instance: SchedulingService?) {
        instance?.close()
    }
}
