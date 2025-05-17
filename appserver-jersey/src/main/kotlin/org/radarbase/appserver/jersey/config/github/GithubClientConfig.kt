package org.radarbase.appserver.jersey.config.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubClientConfig(
    val maxContentLength: Long = 10_00_000,
    @JsonProperty("timeoutSec")
    val timeout: Long = 10L,
    val githubToken: String? = null,
)
