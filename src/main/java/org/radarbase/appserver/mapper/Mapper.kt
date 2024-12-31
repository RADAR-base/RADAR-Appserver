package org.radarbase.appserver.mapper

/**
 * Generic converter class for conversions between entity [org.radarbase.appserver.entity] and
 * DTO [org.radarbase.appserver.dto] objects.
 *
 * @param <E> the entity object class
 * @param <D> the DTO object class
 * TODO - Use MapStruct for mapping entities and DTOs (http://mapstruct.org/)
 */
interface Mapper<D, E> {
    fun dtoToEntity(dto: D): E
    fun entityToDto(entity: E): D
    fun entitiesToDtos(entities: List<E>): List<D> = entities.asSequence().mapTo (ArrayList(), ::entityToDto)
    fun dtosToEntities(dtos: List<D>): List<E> = dtos.asSequence().mapTo (ArrayList(), ::dtoToEntity)
}