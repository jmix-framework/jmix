package io.jmix.oidc.userinfo;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Marker interface for OidcUserService that is used by auto-configuration. If a Spring bean implementing this interface
 * is registered in the application then this bean will be used instead of standard implementation provided by the
 * add-on.
 *
 * @see DefaultJmixOidcUserService
 */
public interface JmixOidcUserService extends OAuth2UserService<OidcUserRequest, OidcUser> {
}