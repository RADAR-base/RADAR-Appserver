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

package org.radarbase.appserver.search

import org.radarbase.appserver.entity.Task
import org.springframework.data.jpa.domain.Specification

class TaskSpecificationsBuilder {

    @Transient
    private val params: MutableList<SearchCriteria> = mutableListOf()

    fun with(key: String, operation: String, value: Any): TaskSpecificationsBuilder {
        params.add(SearchCriteria(key, operation, value))
        return this
    }

    fun build(): Specification<Task>? {
        if (params.isEmpty()) {
            return null
        }

        val specs: List<Specification<Task>> = params.map(::TaskSpecification)

        var result: Specification<Task> = specs[0]

        for (i in 1 until params.size) {
            result = if (params[i].isOrPredicate()) {
                Specification.where(result).or(specs[i])
            } else {
                Specification.where(result).and(specs[i])
            }
        }

        return result
    }
}
