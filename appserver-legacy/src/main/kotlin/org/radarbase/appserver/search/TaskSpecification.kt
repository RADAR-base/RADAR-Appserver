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

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.radarbase.appserver.entity.Task
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class TaskSpecification(private val criteria: SearchCriteria) : Specification<Task> {

    override fun toPredicate(
        root: Root<Task>,
        query: CriteriaQuery<*>?,
        builder: CriteriaBuilder,
    ): Predicate? {
        return when (criteria.operation) {
            ">" -> builder.greaterThanOrEqualTo(root.get(criteria.key), criteria.value.toString())
            "<" -> builder.lessThanOrEqualTo(root.get(criteria.key), criteria.value.toString())
            ":" -> {
                if (root.get<Any>(criteria.key).javaType == String::class.java) {
                    builder.like(root.get(criteria.key), "%${criteria.value}%")
                } else {
                    builder.equal(root.get<Any>(criteria.key), criteria.value)
                }
            }
            else -> null
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID = 327842183571958L
    }
}
