package io.jmix.oidc.user;

import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * An interface to be implemented by a user Principal that is registered with an OpenID Connect 1.0 Provider. The
 * interface extends {@link UserDetails} because Jmix framework requires the user principal put to the {@link
 * org.springframework.security.core.context.SecurityContext} to implement it. Implementations of {@link io.jmix.oidc.usermapper.OidcUserMapper}
 * must return instances of this interface.
 *
 * @see io.jmix.oidc.usermapper.OidcUserMapper
 */
public interface JmixOidcUser extends OidcUser, JmixUserDetails {
}
