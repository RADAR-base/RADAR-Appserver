package org.radarbase.appserver.jersey.factory.quartz

import org.glassfish.hk2.api.ServiceLocator
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

class HK2JobFactory(
    private val serviceLocator: ServiceLocator,
) : JobFactory {

    @Throws(SchedulerException::class)
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        val jobClass = bundle.jobDetail.jobClass
        return serviceLocator.create(jobClass)
    }
}
