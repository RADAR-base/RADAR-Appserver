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

package org.radarbase.appserver.jersey.mapper

import kotlinx.coroutines.Dispatchers
import org.radarbase.appserver.jersey.utils.mapParallel

interface Mapper<D, E> {
    suspend fun dtoToEntity(dto: D): E
    suspend fun entityToDto(entity: E): D

    /** Convert all entities to DTOs in parallel coroutines. */
    suspend fun entitiesToDtos(entities: Iterable<E>): List<D> =
        entities.mapParallel(Dispatchers.Default) { entity ->
            entityToDto(entity)
        }

    /** Convert all DTOs to entities in parallel coroutines. */
    suspend fun dtosToEntities(dtos: Iterable<D>): List<E> =
        dtos.mapParallel(Dispatchers.Default) { dto ->
            dtoToEntity(dto)
        }
}
