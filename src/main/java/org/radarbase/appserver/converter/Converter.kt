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
package org.radarbase.appserver.converter

import java.util.stream.Collectors

/**
 * Generic converter class for conversions between entity [org.radarbase.appserver.entity] and
 * DTO [org.radarbase.appserver.dto] objects.
 *
 * @param <T> the entity object class
 * @param <S> the DTO object class
 * @author yatharthranjan
 *
 * TODO - Use MapStruct for mapping entities and DTOs (http://mapstruct.org/)
</S></T> */
interface Converter<T, S> {
    fun dtoToEntity(s: S): T
    fun entityToDto(t: T): S

    fun dtosToEntities(ss: Collection<S>): List<T> {
        return ss.parallelStream().map { s: S -> dtoToEntity(s) }.collect(Collectors.toList())
    }

    fun entitiesToDtos(ts: Collection<T>): List<S> {
        return ts.parallelStream().map { t: T -> entityToDto(t) }.collect(Collectors.toList())
    }
}