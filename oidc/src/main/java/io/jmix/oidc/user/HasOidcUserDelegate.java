package io.jmix.oidc.user;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface to be implemented by classes that represent application user and that wrap the {@link OidcUser}. Such
 * classes delegate some method invocations to the wrapped {@code oidcUser}. Classes implementing this interface will be
 * handled, for example, in {@link io.jmix.oidc.usermapper.BaseOidcUserMapper}.
 */
public interface HasOidcUserDelegate {

    OidcUser getDelegate();

    void setDelegate(OidcUser delegate);
}
