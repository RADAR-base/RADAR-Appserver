package org.radarbase.appserver.service

import org.radarbase.appserver.util.CachedFunction
import org.radarbase.appserver.util.CustomThrowingFunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.time.Duration

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GithubService (
    @field:Transient private val githubClient: GithubClient,

    @Value("\${security.github.cache.duration:3600}") cacheTime: Int,
    @Value("\${security.github.cache.retryDuration:60}") retryTime: Int,
    @Value("\${security.github.cache.size:10000}") maxSize: Int
) {
    @Transient
    private val cachedGetContent: CachedFunction<String, String> = CachedFunction<String, String>(
        githubClient::getGithubContent,
        Duration.ofSeconds(cacheTime.toLong()),
        Duration.ofSeconds(retryTime.toLong()),
        maxSize
    )

    @Throws(IOException::class, InterruptedException::class)
    fun getGithubContent(url: String): String = try {
        this.cachedGetContent.applyWithException(url)
    } catch (ex: IOException) {
        throw ex
    } catch (ex: ResponseStatusException) {
        throw ex
    } catch (ex: Exception) {
        throw IllegalStateException("Unknown exception $ex", ex)
    }

    @Throws(IOException::class)
    fun getGithubContentWithoutCache(url: String): String {
        return githubClient.getGithubContent(url)
    }
}
