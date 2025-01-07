/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.mapper

import java.util.stream.Collectors

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
    fun entitiesToDtos(entities: Collection<E>): List<D> = entities.parallelStream().map(::entityToDto).collect(Collectors.toList())
    fun dtosToEntities(dtos: Collection<D>): List<E> = dtos.parallelStream().map ( ::dtoToEntity).collect(Collectors.toList())
}