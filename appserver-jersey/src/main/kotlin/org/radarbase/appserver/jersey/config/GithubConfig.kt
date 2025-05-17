package org.radarbase.appserver.jersey.config

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubConfig (
    @field:JsonProperty("cacheDurationSec")
    val cacheDuration: Long,
    @field:JsonProperty("retryDurationSec")
    val retryDuration: Long,
    val maxCacheSize: Long
)
