package io.jmix.oidc.usermapper;

import io.jmix.oidc.user.JmixOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface is responsible for transforming an instance of {@link OidcUser} (from Spring Security) to {@link
 * JmixOidcUser} which may be used by Jmix application. Authorities mapping is often delegated to the {@link
 * io.jmix.oidc.claimsmapper.ClaimsRolesMapper}.
 *
 * @param <T> type of user object used by Jmix application
 */
public interface OidcUserMapper<T extends JmixOidcUser> {

    /**
     * Transforms an object with user information to the instance of the user used by Jmix. Method implementations may
     * also perform users synchronization, e.g. to store users in the database.
     *
     * @param oidcUser the object that stores information about the user received from the OpenID Provider
     * @return an instance of Jmix user that may be set into security context
     */
    T toJmixUser(OidcUser oidcUser);
}
