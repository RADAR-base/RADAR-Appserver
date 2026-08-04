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
            WHERE n.id = :id
            """.trimIndent(),
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
