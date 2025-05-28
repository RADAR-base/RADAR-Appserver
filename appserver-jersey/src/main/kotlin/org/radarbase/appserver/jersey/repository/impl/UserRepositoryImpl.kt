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
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class UserRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), UserRepository {
    override suspend fun find(id: Long): User? = transact {
        find(User::class.java, id)
    }

    override suspend fun findBySubjectId(subjectId: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.subjectId = :subjectId",
            User::class.java,
        ).setParameter("subjectId", subjectId).resultList.firstOrNull()
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(u)
                FROM User u 
                WHERE u.id = :id""".trimIndent(),
            Long::class.java,
        ).setParameter("id", id).singleResult > 0
    }

    override suspend fun existsBySubjectId(subjectId: String): Boolean = findBySubjectId(subjectId) != null

    override suspend fun findByProjectId(projectId: Long): List<User> = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.project.id = :projectId",
            User::class.java,
        )
            .setParameter("projectId", projectId)
            .resultList
    }

    override suspend fun findBySubjectIdAndProjectId(
        subjectId: String,
        projectId: Long,
    ): User? = transact {
        createQuery(
            """SELECT u 
                        FROM User u 
                        WHERE u.subjectId = :subjectId 
                        AND u.project.id = :projectId""".trimIndent(),
            User::class.java,
        )
            .setParameter("subjectId", subjectId)
            .setParameter("projectId", projectId)
            .resultList
            .firstOrNull()

    }

    override suspend fun findByFcmToken(fcmToken: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.fcmToken = :fcmToken",
            User::class.java,
        )
            .setParameter("fcmToken", fcmToken)
            .resultList
            .firstOrNull()
    }

    override suspend fun add(entity: User): User = transact {
        entity.apply(::persist)
    }

    override suspend fun update(entity: User): User? = transact {
        merge(entity)
    }

    override suspend fun delete(entity: User): Unit = transact {
        remove(merge(entity))
    }

    override suspend fun findAll(): List<User> = transact {
        createQuery("SELECT u FROM User u", User::class.java).resultList
    }
}
