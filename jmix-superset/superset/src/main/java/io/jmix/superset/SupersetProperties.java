package io.jmix.superset;

import jakarta.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("jmix.superset")
public class SupersetProperties {

    /**
     * URL where Superset is available.
     */
    String url;

    /**
     * The user name of Superset user that will be used in login request.
     */
    String username;

    /**
     * The password of Superset user that will be used in login request.
     */
    String password;

    /**
     * The schedule delay that is used for monitoring whether access token is expired.
     */
    Duration accessTokenRefreshSchedule;

    /**
     * The schedule delay that is used for getting new CSRF token. Unlike access token, CSRF token does not contain
     * information about expiration time, so the schedule delay should almost equal to CSRF token lifespan. By default,
     * Superset configures 1 week for CSRF, so the default value of {@code csrfTokenRefreshSchedule} property is 6d and 23h.
     * <p>
     * If the value of CSRF token expiration time is changed in Superset, this property should be changed accordingly.
     */
    Duration csrfTokenRefreshSchedule;

    /**
     * Enables CSRF protection. CSRF token will be taken on Spring context refresh and will be sent in a guest token
     * request.
     */
    boolean csrfProtectionEnabled;

    public SupersetProperties(String url,
                              String username,
                              String password,
                              @DefaultValue("1m") Duration accessTokenRefreshSchedule,
                              @DefaultValue("true") boolean csrfProtectionEnabled,
                              @DefaultValue("167h") Duration csrfTokenRefreshSchedule) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.accessTokenRefreshSchedule = accessTokenRefreshSchedule;
        this.csrfProtectionEnabled = csrfProtectionEnabled;
        this.csrfTokenRefreshSchedule = csrfTokenRefreshSchedule;
    }

    /**
     * @return Superset URL or {@code null} if not specified
     * @see #url
     */
    @Nullable
    public String getUrl() {
        return url;
    }

    /**
     * @return user name of Superset user or {@code null} if not specified
     * @see #username
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * @return password of Superset user or {@code null} if not specified
     * @see #password
     */
    @Nullable
    public String getPassword() {
        return password;
    }

    /**
     * @return access token refresh schedule delay
     * @see #accessTokenRefreshSchedule
     */
    public Duration getAccessTokenRefreshSchedule() {
        return accessTokenRefreshSchedule;
    }

    /**
     * @return {@code true} if CSRF protection is enabled
     * @see #csrfProtectionEnabled
     */
    public boolean isCsrfProtectionEnabled() {
        return csrfProtectionEnabled;
    }

    /**
     * @return schedule delay that is used for getting new CSRF token
     * @see #csrfTokenRefreshSchedule
     */
    public Duration getCsrfTokenRefreshSchedule() {
        return csrfTokenRefreshSchedule;
    }
}
