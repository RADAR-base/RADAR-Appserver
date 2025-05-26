package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.DataMessageStateEvent
import org.radarbase.appserver.jersey.repository.DataMessageStateEventRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class DataMessageStateEventRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), DataMessageStateEventRepository {
    override suspend fun find(id: Long): DataMessageStateEvent? = transact {
        find(DataMessageStateEvent::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = find(id) != null

    override suspend fun add(entity: DataMessageStateEvent): DataMessageStateEvent = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: DataMessageStateEvent) = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<DataMessageStateEvent> = transact {
        createQuery(
            "SELECT d from DataMessageStateEvent d",
            DataMessageStateEvent::class.java,
        ).resultList
    }

    override suspend fun update(entity: DataMessageStateEvent): DataMessageStateEvent? = transact {
        merge(entity)
    }

    override suspend fun findByDataMessageId(dataMessageId: Long): List<DataMessageStateEvent> = transact {
        createQuery(
            """SELECT d 
                FROM DataMessageStateEvent d 
                WHERE d.dataMessage.id = :dataMessageId
            """.trimIndent(),
            DataMessageStateEvent::class.java,
        ).setParameter("dataMessageId", dataMessageId)
            .resultList
    }

    override suspend fun countByDataMessageId(dataMessageId: Long): Long = transact {
        createQuery(
            """select count(d) 
                from DataMessageStateEvent d 
                where d.dataMessage.id = :dataMessageId
            """.trimIndent(),
            Long::class.java,
        ).setParameter("dataMessageId", dataMessageId)
            .singleResult
    }
}
