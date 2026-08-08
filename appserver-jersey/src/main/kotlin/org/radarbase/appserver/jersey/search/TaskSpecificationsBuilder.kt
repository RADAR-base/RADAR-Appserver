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

package org.radarbase.appserver.jersey.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.radarbase.appserver.jersey.entity.Task

class TaskSpecificationsBuilder {
    private val params = mutableListOf<SearchCriteria>()

    fun with(key: String, op: String, value: Any): TaskSpecificationsBuilder {
        params.add(SearchCriteria(key, op, value))
        return this
    }

    fun build(): QuerySpecification<Task> {
        return QuerySpecification { root: Root<Task>, query: CriteriaQuery<*>, builder: CriteriaBuilder ->
            if (params.isEmpty()) return@QuerySpecification builder.conjunction()

            val predicates: List<Predicate> = params.map {
                TaskSpecification(it).toPredicate(root, query, builder)
            }

            var result = predicates.first()
            for (i in 1 until predicates.size) {
                result = if (params[i].isOrPredicate()) {
                    builder.or(result, predicates[i])
                } else {
                    builder.and(result, predicates[i])
                }
            }
            result
        }
    }
}
