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

package org.radarbase.appserver.microservices.cloud.messaging.repository

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import org.radarbase.appserver.microservices.core.entity.DataMessageStateEvent
import org.radarbase.appserver.microservices.core.repository.DataMessageStateEventRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class DataMessageStateEventRepositoryImpl @Inject constructor(
    em: Provider<EntityManager>,
    asyncCoroutineService: AsyncCoroutineService,
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
