package io.jmix.superset;

import jakarta.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("jmix.superset")
public class SupersetProperties {

    String url;
    String username;
    String password;
    Duration refreshAccessTokenScheduler;
    Duration fallbackAccessTokenExpiration;

    public SupersetProperties(String url,
                              String username,
                              String password,
                              @DefaultValue("1m") Duration refreshAccessTokenScheduler,
                              @DefaultValue("3m") Duration fallbackAccessTokenExpiration) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.refreshAccessTokenScheduler = refreshAccessTokenScheduler;
        this.fallbackAccessTokenExpiration = fallbackAccessTokenExpiration;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public Duration getRefreshAccessTokenScheduler() {
        return refreshAccessTokenScheduler;
    }

    public Duration getFallbackAccessTokenExpiration() {
        return fallbackAccessTokenExpiration;
    }
}
