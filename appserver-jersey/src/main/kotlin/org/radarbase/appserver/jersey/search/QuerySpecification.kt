package org.radarbase.appserver.jersey.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

fun interface QuerySpecification<T> {
    fun toPredicate(root: Root<T>, criteriaQuery: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate
}
