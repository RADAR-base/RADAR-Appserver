package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.TaskStateEvent
import org.radarbase.appserver.jersey.repository.TaskStateEventRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class TaskStateEventRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : TaskStateEventRepository, HibernateRepository(em, asyncCoroutineService) {
    override suspend fun find(id: Long): TaskStateEvent? = transact {
        find(TaskStateEvent::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(e) 
                FROM TaskStateEvent e
                WHERE e.id = :id""".trimIndent(),
            Long::class.java,
        ).setParameter("id", id)
            .singleResult > 0
    }

    override suspend fun add(entity: TaskStateEvent): TaskStateEvent = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: TaskStateEvent) = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<TaskStateEvent> = transact {
        createQuery("SELECT e FROM TaskStateEvent e", TaskStateEvent::class.java)
            .resultList
    }

    override suspend fun update(entity: TaskStateEvent): TaskStateEvent? = transact {
        merge(entity)
    }

    override suspend fun findByTaskId(taskId: Long): List<TaskStateEvent> = transact {
        createQuery(
            "SELECT e FROM TaskStateEvent e WHERE e.task.id = :taskId",
            TaskStateEvent::class.java,
        ).setParameter("taskId", taskId)
            .resultList

    }

    override suspend fun countByTaskId(taskId: Long): Long = transact {
        createQuery("SELECT COUNT(e) FROM TaskStateEvent e WHERE e.task.id = :taskId", Long::class.java)
            .setParameter("taskId", taskId)
            .singleResult
    }
}
