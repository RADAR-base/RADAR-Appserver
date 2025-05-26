package org.radarbase.appserver.jersey.search

data class SearchCriteria(
    val key: String,
    val operation: String,
    val value: Any
) {
    /***
     * Only AND supported in the first instance. Later we can add a new query param that can provide this value
     */
    fun isOrPredicate(): Boolean = false
}
