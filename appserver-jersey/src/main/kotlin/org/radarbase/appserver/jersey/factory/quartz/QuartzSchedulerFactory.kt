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

package org.radarbase.appserver.jersey.factory.quartz

import jakarta.inject.Inject
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class QuartzSchedulerFactory @Inject constructor(
    private val serviceLocator: ServiceLocator,
) : DisposableSupplier<Scheduler> {
    val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
    var scheduler: Scheduler? = null

    override fun get(): Scheduler {
        logger.info("Retrieving quartz scheduler instance")
        return scheduler.let { sch ->
            if (sch == null || sch.isShutdown) {
                scheduler = schedulerFactory.scheduler.also {
                    if (!it.isStarted) {
                        it.start()
                    }
                    it.setJobFactory(HK2JobFactory(serviceLocator))
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
