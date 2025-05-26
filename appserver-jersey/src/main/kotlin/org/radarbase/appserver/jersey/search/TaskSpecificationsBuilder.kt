package org.radarbase.appserver.jersey.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
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

            val predicates = params.map { TaskSpecification(it).toPredicate(root, query, builder) }

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
