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

package org.radarbase.appserver.microservices.notification.repository

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.repository.NotificationRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import java.time.Instant

class NotificationRepositoryImpl @Inject constructor(
    em: Provider<EntityManager>,
    asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), NotificationRepository {

    override suspend fun find(id: Long): Notification? = transact {
        find(Notification::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = transact {
        val count = createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.id = :id",
            Long::class.java,
        )
            .setParameter("id", id)
            .singleResult
        count > 0
    }

    override suspend fun add(entity: Notification): Notification = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: Notification): Unit = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<Notification> = transact {
        createQuery("SELECT n FROM Notification n", Notification::class.java)
            .resultList
    }

    override suspend fun update(entity: Notification): Notification = transact {
        merge(entity)
    }

    override suspend fun findByUserId(userId: Long): List<Notification> = transact {
        createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId",
            Notification::class.java,
        ).setParameter("userId", userId)
            .resultList
    }

    override suspend fun findByIdAndUserId(id: Long, userId: Long): Notification? = transact {
        createQuery(
            "SELECT n FROM Notification n WHERE n.id = :id AND n.userId = :userId",
            Notification::class.java,
        ).setParameter("id", id)
            .setParameter("userId", userId)
            .resultList
            .firstOrNull()
    }

    override suspend fun findByUserIdAndTaskId(userId: Long, taskId: Long): List<Notification> = transact {
        createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId AND n.taskId = :taskId",
            Notification::class.java,
        ).setParameter("userId", userId)
            .setParameter("taskId", taskId)
            .resultList
    }

    override suspend fun findByTaskId(id: Long): List<Notification> = transact {
        createQuery(
            "SELECT n FROM Notification n WHERE n.taskId = :taskId",
            Notification::class.java,
        ).setParameter("taskId", id)
            .resultList
    }

    override suspend fun findByFcmMessageId(fcmMessageId: String): Notification? = transact {
        createQuery(
            "SELECT n FROM Notification n WHERE n.fcmMessageId = :fcmMessageId",
            Notification::class.java,
        ).setParameter("fcmMessageId", fcmMessageId)
            .resultList
            .firstOrNull()
    }

    override suspend fun findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        title: String,
        body: String,
        type: String,
        ttlSeconds: Int,
    ): Notification? = transact {
        createQuery(
            """SELECT n FROM Notification n
            WHERE n.userId = :userId
              AND n.sourceId = :sourceId
              AND n.scheduledTime = :scheduledTime
              AND n.title = :title
              AND n.body = :body
              AND n.type = :type
              AND n.ttlSeconds = :ttlSeconds""",
            Notification::class.java,
        ).setParameter("userId", userId)
            .setParameter("sourceId", sourceId)
            .setParameter("scheduledTime", scheduledTime)
            .setParameter("title", title)
            .setParameter("body", body)
            .setParameter("type", type)
            .setParameter("ttlSeconds", ttlSeconds)
            .resultList
            .firstOrNull()
    }

    override suspend fun existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        title: String,
        body: String,
        type: String,
        ttlSeconds: Int,
    ): Boolean = transact {
        val count = createQuery(
            """SELECT COUNT(n)
            FROM Notification n
            WHERE n.userId = :userId
              AND n.sourceId = :sourceId
              AND n.scheduledTime = :scheduledTime
              AND n.title = :title
              AND n.body = :body
              AND n.type = :type
              AND n.ttlSeconds = :ttlSeconds""",
            Long::class.java,
        ).setParameter("userId", userId)
            .setParameter("sourceId", sourceId)
            .setParameter("scheduledTime", scheduledTime)
            .setParameter("title", title)
            .setParameter("body", body)
            .setParameter("type", type)
            .setParameter("ttlSeconds", ttlSeconds)
            .singleResult
        count > 0
    }

    override suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(n) 
                FROM Notification n 
                WHERE n.id = :id 
                AND n.userId = :userId
            """.trimIndent(),
            Long::class.java,
        ).setParameter("id", id)
            .setParameter("userId", userId)
            .singleResult > 0
    }

    override suspend fun existsByTaskId(taskId: Long): Boolean = transact {
        val count = createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.taskId = :taskId",
            Long::class.java,
        ).setParameter("taskId", taskId)
            .singleResult
        count > 0
    }

    override suspend fun deleteByUserId(userId: Long): Unit = transact {
        createQuery(
            "DELETE FROM Notification n WHERE n.userId = :userId",
        ).setParameter("userId", userId)
            .executeUpdate()
    }

    override suspend fun deleteByTaskId(taskId: Long): Unit = transact {
        createQuery(
            "DELETE FROM Notification n WHERE n.taskId = :taskId",
        ).setParameter("taskId", taskId)
            .executeUpdate()
    }

    override suspend fun deleteByUserIdAndTaskId(userId: Long, taskId: Long): Unit = transact {
        createQuery(
            "DELETE FROM Notification n WHERE n.userId = :userId AND n.taskId = :taskId",
        ).setParameter("userId", userId)
            .setParameter("taskId", taskId)
            .executeUpdate()
    }

    override suspend fun deleteByFcmMessageId(fcmMessageId: String): Unit = transact {
        createQuery(
            "DELETE FROM Notification n WHERE n.fcmMessageId = :fcmMessageId",
        ).setParameter("fcmMessageId", fcmMessageId)
            .executeUpdate()
    }

    override suspend fun deleteByIdAndUserId(id: Long, userId: Long): Unit = transact {
        createQuery(
            "DELETE FROM Notification n WHERE n.id = :id AND n.userId = :userId",
        ).setParameter("id", id)
            .setParameter("userId", userId)
            .executeUpdate()
    }
}
