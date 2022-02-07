package io.jmix.oidc.user;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface to be implemented by classes that represent application users and that wraps the {@link OidcUser}
 * delegating sme method invocations to the wrapped {@code oidcUser}
 */
public interface HasOidcUserDelegate {
    
    OidcUser getDelegate();

    void setDelegate(OidcUser delegate);
}
