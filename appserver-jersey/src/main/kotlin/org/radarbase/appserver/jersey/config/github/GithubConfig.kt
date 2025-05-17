package org.radarbase.appserver.jersey.config.github

data class GithubConfig(
    val cache: GithubCacheConfig = GithubCacheConfig(),
    val client: GithubClientConfig = GithubClientConfig()
)
