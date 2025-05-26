package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.NotificationStateEvent
import org.radarbase.appserver.jersey.repository.NotificationStateEventRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class NotificationStateEventRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), NotificationStateEventRepository {
    override suspend fun find(id: Long): NotificationStateEvent? = transact {
        find(NotificationStateEvent::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(n) 
            FROM NotificationStateEvent n 
            WHERE n.id = :id""".trimIndent(),
            Long::class.java,
        ).setParameter("id", id)
            .singleResult > 0
    }

    override suspend fun add(entity: NotificationStateEvent): NotificationStateEvent = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: NotificationStateEvent) = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<NotificationStateEvent> = transact {
        createQuery(
            "SELECT n FROM NotificationStateEvent n",
            NotificationStateEvent::class.java,
        ).resultList
    }

    override suspend fun update(entity: NotificationStateEvent): NotificationStateEvent? = transact {
        merge(entity)
    }

    override suspend fun findByNotificationId(notificationId: Long): List<NotificationStateEvent> = transact {
        createQuery(
            """SELECT n FROM NotificationStateEvent n WHERE n.notification.id = :notificationId""",
            NotificationStateEvent::class.java,
        ).setParameter("notificationId", notificationId)
            .resultList
    }

    override suspend fun countByNotificationId(notificationId: Long): Long = transact {
        createQuery(
            """SELECT COUNT(n) FROM NotificationStateEvent n WHERE n.notification.id = :notificationId""",
            Long::class.java,
        ).setParameter("notificationId", notificationId)
            .singleResult
    }
}
