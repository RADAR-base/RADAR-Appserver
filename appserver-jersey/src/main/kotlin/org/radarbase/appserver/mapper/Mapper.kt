package org.radarbase.appserver.mapper

import kotlinx.coroutines.Dispatchers
import org.radarbase.kotlin.coroutines.forkJoin

interface Mapper<D, E> {
    suspend fun dtoToEntity(dto: D): E
    suspend fun entityToDto(entity: E): D

    /** Convert all entities to DTOs in parallel coroutines. */
    suspend fun entitiesToDtos(entities: Iterable<E>): List<D> =
        entities.forkJoin(Dispatchers.Default) { entity ->
            entityToDto(entity)
        }

    /** Convert all DTOs to entities in parallel coroutines. */
    suspend fun dtosToEntities(dtos: Iterable<D>): List<E> =
        dtos.forkJoin(Dispatchers.Default) { dto ->
            dtoToEntity(dto)
        }
}
