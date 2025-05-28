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
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.repository.TaskRepository
import org.radarbase.appserver.jersey.search.QuerySpecification
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import java.sql.Timestamp

class TaskRepositoryImpl (
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), TaskRepository{
    override suspend fun find(id: Long): Task? = transact {
        find(Task::class.java, id)
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            "SELECT COUNT(t) FROM Task t WHERE t.id = :id",
            Long::class.java
        ).setParameter("id", id)
            .singleResult > 0
    }

    override suspend fun add(entity: Task): Task = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: Task) = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<Task> = transact {
        createQuery("SELECT t FROM Task t", Task::class.java)
            .resultList
    }

    override suspend fun update(entity: Task): Task? = transact {
        merge(entity)
    }

    override suspend fun findByUserId(userId: Long): List<Task> = transact {
        createQuery(
            "SELECT t FROM Task t WHERE t.user.id = :userId",
            Task::class.java
        ).setParameter("userId", userId)
            .resultList
    }

    override suspend fun findByUserIdAndType(
        userId: Long,
        type: AssessmentType,
    ): List<Task> = transact {
        createQuery(
            "SELECT t FROM Task t WHERE t.user.id = :userId AND t.type = :type",
            Task::class.java
        )
            .setParameter("userId", userId)
            .setParameter("type", type)
            .resultList
    }

    override suspend fun deleteByUserId(userId: Long): Unit = transact {
        createQuery("DELETE FROM Task t WHERE t.user.id = :userId")
            .setParameter("userId", userId)
            .executeUpdate()
    }

    override suspend fun deleteByUserIdAndType(
        userId: Long,
        type: AssessmentType,
    ): Unit = transact {
        createQuery(
            "DELETE FROM Task t WHERE t.user.id = :userId AND t.type = :type"
        )
            .setParameter("userId", userId)
            .setParameter("type", type)
            .executeUpdate()
    }

    override suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean = transact {
        createQuery(
            "SELECT COUNT(t) FROM Task t WHERE t.id = :id AND t.user.id = :userId",
            Long::class.java
        )
            .setParameter("id", id)
            .setParameter("userId", userId)
            .singleResult > 0
    }

    override suspend fun existsByUserIdAndNameAndTimestamp(
        userId: Long,
        name: String,
        timestamp: Timestamp,
    ): Boolean = transact {
        createQuery(
            "SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.name = :name AND t.timestamp = :timestamp",
            Long::class.java
        )
            .setParameter("userId", userId)
            .setParameter("name", name)
            .setParameter("timestamp", timestamp)
            .singleResult > 0
    }

    override suspend fun findByIdAndUserId(id: Long, userId: Long): Task? = transact {
        createQuery(
            "SELECT t FROM Task t WHERE t.id = :id AND t.user.id = :userId",
            Task::class.java
        )
            .setParameter("id", id)
            .setParameter("userId", userId)
            .resultList
            .firstOrNull()
    }

    override suspend fun findAll(specification: QuerySpecification<Task>): List<Task> = transact {
        val cb: CriteriaBuilder = criteriaBuilder
        val cq: CriteriaQuery<Task> = cb.createQuery(Task::class.java)
        val root: Root<Task> = cq.from(Task::class.java)
        val predicate: Predicate = specification.toPredicate(root, cq, cb)
        cq.select(root).where(predicate)
        createQuery(cq).resultList
    }

    override suspend fun deleteAll(tasks: List<Task>) = transact {
        tasks.forEach { remove(merge(it)) }
    }
}
