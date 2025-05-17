package org.radarbase.appserver.jersey.config.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubCacheConfig(
    @field:JsonProperty("cacheDurationSec")
    val cacheDuration: Long? = null,
    @field:JsonProperty("retryDurationSec")
    val retryDuration: Long? = null,
    val maxCacheSize: Long = 10000L,
)
