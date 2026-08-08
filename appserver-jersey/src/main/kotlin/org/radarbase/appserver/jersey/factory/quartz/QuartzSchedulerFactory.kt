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
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties

class QuartzSchedulerFactory @Inject constructor(
    private val serviceLocator: ServiceLocator,
    appserverConfig: AppserverConfig,
) : DisposableSupplier<Scheduler> {
    val schedulerFactory: SchedulerFactory = StdSchedulerFactory(buildQuartzProperties(appserverConfig))
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

        private const val DATA_SOURCE_NAME = "appserverDS"

        private fun buildQuartzProperties(config: AppserverConfig): Properties {
            val db = config.db
            val quartz = config.quartz
            return Properties().apply {
                setProperty("org.quartz.scheduler.instanceName", quartz.instanceName)
                setProperty("org.quartz.scheduler.instanceId", "AUTO")
                setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool")
                setProperty("org.quartz.threadPool.threadCount", quartz.threadCount.toString())
                setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX")
                setProperty("org.quartz.jobStore.driverDelegateClass", quartz.driverDelegateClass)
                setProperty("org.quartz.jobStore.tablePrefix", quartz.tablePrefix)
                setProperty("org.quartz.jobStore.dataSource", DATA_SOURCE_NAME)
                setProperty("org.quartz.jobStore.misfireThreshold", quartz.misfireThreshold.toString())
                val prefix = "org.quartz.dataSource.$DATA_SOURCE_NAME"
                setProperty("$prefix.provider", "hikaricp")
                setProperty("$prefix.driver", db.jdbcDriver)
                setProperty("$prefix.URL", db.jdbcUrl)
                setProperty("$prefix.user", db.username)
                setProperty("$prefix.password", db.password)
                setProperty("$prefix.maxConnections", (quartz.threadCount + 2).toString())
            }
        }
    }
}
