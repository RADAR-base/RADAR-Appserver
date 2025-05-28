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

package org.radarbase.appserver.jersey.repository

interface BaseRepository<T> {
    suspend fun find(id: Long): T?
    suspend fun exists(id: Long): Boolean
    suspend fun add(entity: T): T
    suspend fun delete(entity: T)
    suspend fun findAll(): List<T>
    suspend fun update(entity: T): T?
}
