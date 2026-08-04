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

package org.radarbase.appserver.jersey.service.scheduling

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class SchedulingService : Closeable {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun execute(method: () -> Unit) = scheduler.execute(method)

    fun repeat(rate: Duration, initialDelay: Duration, method: () -> Unit): RepeatReference {
        val ref = scheduler.scheduleAtFixedRate(method, initialDelay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS)
        return FutureRepeatReference(ref)
    }

    interface RepeatReference : Closeable

    private inner class FutureRepeatReference(private val ref: ScheduledFuture<*>) : RepeatReference {
        override fun close() {
            if (ref.cancel(false)) {
                try {
                    ref.get()
                } catch (ex: CancellationException) {
                    // this is expected
                } catch (ex: Exception) {
                    logger.warn("Failed to get repeating job result", ex)
                }
            }
        }
    }

    override fun close() {
        scheduler.shutdown() // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!scheduler.awaitTermination(TERMINATION_INTERVAL_SECONDS, TimeUnit.SECONDS)) {
                scheduler.shutdownNow() // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!scheduler.awaitTermination(TERMINATION_INTERVAL_SECONDS, TimeUnit.SECONDS)) {
                    logger.error("SchedulingService did not terminate")
                }
            }
        } catch (ie: InterruptedException) {
            // (Re-)Cancel if current thread also interrupted
            scheduler.shutdownNow()
            // Preserve interrupt status
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchedulingService::class.java)

        private const val TERMINATION_INTERVAL_SECONDS = 60L
    }
}
