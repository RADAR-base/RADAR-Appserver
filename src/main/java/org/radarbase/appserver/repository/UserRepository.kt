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
package org.radarbase.appserver.repository

import org.radarbase.appserver.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.stereotype.Repository
import java.util.*
import javax.validation.constraints.NotNull

/** @author yatharthranjan
 */
@Repository
@RepositoryRestResource(exported = false)
interface UserRepository : JpaRepository<User?, Long?> {
    fun findBySubjectId(subjectId: String?): Optional<User?>?
    fun findByProjectId(projectId: Long?): List<User?>?
    fun findBySubjectIdAndProjectId(subjectId: String?, projectId: Long?): Optional<User?>?
    fun findByFcmToken(fcmToken: String?): Optional<User?>?
    override fun deleteById(id: @NotNull Long?)
}