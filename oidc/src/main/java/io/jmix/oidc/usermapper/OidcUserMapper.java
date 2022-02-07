package io.jmix.oidc.usermapper;

import io.jmix.oidc.user.OidcUserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface is responsible for transforming an instance of {@link OidcUser} to {@link OidcUserDetails} that may be used
 * by Jmix application.
 *
 * @param <T> type of user object used by Jmix application
 */
public interface OidcUserMapper<T extends OidcUserDetails> {

    T toJmixUser(OidcUser oidcUser);
}
