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
    Duration accessTokenExpiration;

    public SupersetProperties(String url,
                              String username,
                              String password,
                              @DefaultValue("14m") Duration accessTokenExpiration) { // todo rp check default value
        this.url = url;
        this.username = username;
        this.password = password;
        this.accessTokenExpiration = accessTokenExpiration;
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

    // todo rp nullable?
    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

}
