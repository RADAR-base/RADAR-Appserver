package org.radarbase.appserver.service;

import org.radarbase.appserver.util.CachedFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GithubService {

    private final transient CachedFunction<String, String> cachedGetContent;
    private final transient GithubClient githubClient;

    @Autowired
    public GithubService(
            GithubClient githubClient,
            @Value("${security.github.cache.duration:3600}")
            int cacheTime,
            @Value("${security.github.cache.retryDuration:60}")
            int retryTime,
            @Value("${security.github.cache.size:10000}")
            int maxSize) {
        this.githubClient = githubClient;
        this.cachedGetContent = new CachedFunction<>(githubClient::getGithubContent,
                Duration.ofSeconds(cacheTime),
                Duration.ofSeconds(retryTime),
                maxSize);
    }

    public String getGithubContent(String url) throws IOException, InterruptedException {
        try {
            return this.cachedGetContent.applyWithException(url);
        } catch (IOException | InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unknown exception " + ex, ex);
        }
    }

    public String getGithubContentWithoutCache(String url) throws IOException, InterruptedException {
        return githubClient.getGithubContent(url);
    }
}
