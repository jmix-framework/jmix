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
    Duration accessTokenRefreshSchedule;
    Duration fallbackAccessTokenExpiration;
    Duration csrfTokenRefreshSchedule; // default is 6d and 23h
    boolean csrfProtectionEnabled;

    public SupersetProperties(String url,
                              String username,
                              String password,
                              @DefaultValue("1m") Duration accessTokenRefreshSchedule,
                              @DefaultValue("3m") Duration fallbackAccessTokenExpiration,
                              @DefaultValue("true") boolean csrfProtectionEnabled,
                              @DefaultValue("167h") Duration csrfTokenRefreshSchedule) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.accessTokenRefreshSchedule = accessTokenRefreshSchedule;
        this.fallbackAccessTokenExpiration = fallbackAccessTokenExpiration;
        this.csrfProtectionEnabled = csrfProtectionEnabled;
        this.csrfTokenRefreshSchedule = csrfTokenRefreshSchedule;
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

    public Duration getAccessTokenRefreshSchedule() {
        return accessTokenRefreshSchedule;
    }

    public Duration getFallbackAccessTokenExpiration() {
        return fallbackAccessTokenExpiration;
    }

    public boolean isCsrfProtectionEnabled() {
        return csrfProtectionEnabled;
    }

    public Duration getCsrfTokenRefreshSchedule() {
        return csrfTokenRefreshSchedule;
    }
}
