package org.radarbase.appserver.microservices.contract.utils

object Utils {
    fun normalizedPath(path: String?): String {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isBlank()) return ""
        return "/" + trimmed.trim('/').trim()
    }

    fun normalizedUri(uri: String): String {
        val trimmed = uri.trim()
        return if (trimmed.endsWith("/")) trimmed.removeSuffix("/") else trimmed
    }
}
