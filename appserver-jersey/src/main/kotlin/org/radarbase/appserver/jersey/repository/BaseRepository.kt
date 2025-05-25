package org.radarbase.appserver.jersey.repository

interface BaseRepository<T> {
    suspend fun find(id: Long): T?
    suspend fun exists(id: Long): Boolean
    suspend fun add(entity: T): T
    suspend fun delete(entity: T)
    suspend fun findAll(): List<T>
    suspend fun update(entity: T): T?
}
