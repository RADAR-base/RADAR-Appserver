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

package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.repository.DataMessageRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import java.time.Instant

class DataMessageRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), DataMessageRepository {
    override suspend fun find(id: Long): DataMessage? = transact {
        find(DataMessage::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(d)
                FROM DataMessage d 
                WHERE d.id = :id 
            """.trimIndent(),
            Long::class.java,
        ).setParameter("id", id).singleResult > 0
    }

    override suspend fun add(entity: DataMessage): DataMessage = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: DataMessage) = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<DataMessage> = transact {
        createQuery("SELECT d FROM DataMessage d", DataMessage::class.java).resultList
    }

    override suspend fun update(entity: DataMessage): DataMessage? = transact {
        merge(entity)
    }

    override suspend fun findByUserId(userId: Long): List<DataMessage> = transact {
        createQuery(
            "SELECT d FROM DataMessage d WHERE d.user.id = :userId",
            DataMessage::class.java,
        )
            .setParameter("userId", userId)
            .resultList
    }

    override suspend fun findByIdAndUserId(
        id: Long,
        userId: Long,
    ): DataMessage? = transact {
        createQuery(
            "SELECT d FROM DataMessage d WHERE d.id = :id AND d.user.id = :userId",
            DataMessage::class.java,
        )
            .setParameter("id", id)
            .setParameter("userId", userId)
            .resultList
            .firstOrNull()
    }

    override suspend fun findByFcmMessageId(fcmMessageId: String): DataMessage? = transact {
        createQuery("SELECT d FROM DataMessage d WHERE d.fcmMessageId = :fcmMessageId", DataMessage::class.java)
            .setParameter("fcmMessageId", fcmMessageId)
            .resultList
            .firstOrNull()
    }

    override suspend fun existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        ttlSeconds: Int,
    ): Boolean = transact {
        createQuery(
            """SELECT COUNT(d) 
            FROM DataMessage d 
            WHERE d.user.id = :userId 
            AND d.sourceId = :sourceId 
            AND d.scheduledTime = :scheduledTime 
            AND d.ttlSeconds = :ttlSeconds""",
            Long::class.java,
        )
            .setParameter("userId", userId)
            .setParameter("sourceId", sourceId)
            .setParameter("scheduledTime", scheduledTime)
            .setParameter("ttlSeconds", ttlSeconds)
            .singleResult > 0
    }

    override suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(d)
                FROM DataMessage d 
                WHERE d.id = :id
                AND d.user.id = :userId
            """.trimIndent(),
            Long::class.java,
        )
            .setParameter("id", id)
            .setParameter("userId", userId)
            .singleResult > 0
    }

    override suspend fun deleteByUserId(userId: Long): Unit = transact {
        createQuery("DELETE FROM DataMessage d WHERE d.user.id = :userId")
            .setParameter("userId", userId)
            .executeUpdate()
    }

    override suspend fun deleteByIdAndUserId(id: Long, userId: Long): Unit = transact {
        createQuery(
            "DELETE FROM DataMessage d " +
                "WHERE d.id = :id " +
                "AND d.user.id = :userId",
        )
            .setParameter("id", id)
            .setParameter("userId", userId)
            .executeUpdate()
    }

    override suspend fun deleteByFcmMessageId(fcmMessageId: String): Unit = transact {
        createQuery("DELETE FROM DataMessage d WHERE d.fcmMessageId = :fcmMessageId")
            .setParameter("fcmMessageId", fcmMessageId)
            .executeUpdate()
    }
}
