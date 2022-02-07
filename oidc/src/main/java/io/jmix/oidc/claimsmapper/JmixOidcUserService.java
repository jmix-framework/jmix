package io.jmix.oidc.claimsmapper;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Marker interface for OidcUserService that is used by auto-configuration.
 */
public interface JmixOidcUserService extends OAuth2UserService<OidcUserRequest, OidcUser> {
}