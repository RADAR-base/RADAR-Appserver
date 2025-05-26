package org.radarbase.appserver.jersey.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.radarbase.appserver.jersey.entity.Task

class TaskSpecification(
    private val searchCriteria: SearchCriteria
) : QuerySpecification<Task>{

    override fun toPredicate(
        root: Root<Task>,
        criteriaQuery: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
    ): Predicate {
        val path = root.get<Any>(searchCriteria.key)
        return when (searchCriteria.operation) {
            ">" -> criteriaBuilder.greaterThanOrEqualTo(
                root.get(searchCriteria.key),
                searchCriteria.value as Comparable<Any>
            )

            "<" -> criteriaBuilder.lessThanOrEqualTo(
                root.get(searchCriteria.key),
                searchCriteria.value as Comparable<Any>
            )

            ":" -> if (path.javaType == String::class.java) {
                criteriaBuilder.like(
                    root.get(searchCriteria.key),
                    "%${searchCriteria.value}%"
                )
            } else {
                criteriaBuilder.equal(path, searchCriteria.value)
            }

            else -> throw IllegalArgumentException("Unknown operation: ${searchCriteria.operation}")
        }

    }
}
