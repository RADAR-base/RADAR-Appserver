package org.radarbase.appserver.service;

import org.radarbase.appserver.util.FunctionCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GithubService {

    private final transient FunctionCache<String, String> cache;

    @Autowired
    public GithubService(
            GithubClient githubClient,
            @Value("${security.github.cache.duration:PT1H}")
            Duration cacheTime,
            @Value("${security.github.cache.retryDuration:PT1M}")
            Duration retryTime,
            @Value("${security.github.cache.size:10000}")
            int maxSize) {
        this.cache = new FunctionCache<>(githubClient::getGithubContent, cacheTime, retryTime, maxSize);
    }

    public String getGithubContent(String url) throws Exception {
        return this.cache.getOrThrow(url);
    }
}
