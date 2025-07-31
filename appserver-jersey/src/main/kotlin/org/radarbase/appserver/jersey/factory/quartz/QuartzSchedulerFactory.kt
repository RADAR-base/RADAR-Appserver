package org.radarbase.appserver.jersey.factory.quartz

import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class QuartzSchedulerFactory : DisposableSupplier<Scheduler> {
    val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
    var scheduler: Scheduler? = null

    override fun get(): Scheduler {
        logger.info("Retrieving quartz scheduler instance")
        return scheduler.let {
            if (it == null || it.isShutdown) {
                scheduler = schedulerFactory.scheduler.apply {
                    start()
                }
            }
            scheduler!!
        }
    }

    override fun dispose(instance: Scheduler?) {
        logger.info("Disposing quartz scheduler")
        scheduler?.shutdown()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(QuartzSchedulerFactory::class.java)
    }
}
