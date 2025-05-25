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
) : DataMessageRepository, HibernateRepository(em, asyncCoroutineService) {
    override suspend fun find(id: Long): DataMessage? = transact {
        find(DataMessage::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = find(id) != null

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
            """
            SELECT COUNT(d) 
            FROM DataMessage d 
            WHERE d.user.id = :userId 
            AND d.sourceId = :sourceId 
            AND d.scheduledTime = :scheduledTime 
            AND d.ttlSeconds = :ttlSeconds
    """,
            Long::class.java,
        )
            .setParameter("userId", userId)
            .setParameter("sourceId", sourceId)
            .setParameter("scheduledTime", scheduledTime)
            .setParameter("ttlSeconds", ttlSeconds)
            .singleResult > 0

    }

    override suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean = findByIdAndUserId(id, userId) != null

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
