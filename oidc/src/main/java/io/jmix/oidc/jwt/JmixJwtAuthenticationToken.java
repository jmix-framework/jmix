package io.jmix.oidc.jwt;

import io.jmix.oidc.user.JmixOidcUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * An implementation of an AbstractOAuth2TokenAuthenticationToken representing a Jwt Authentication.
 */
public class JmixJwtAuthenticationToken extends AbstractAuthenticationToken {

    protected JmixOidcUser principal;

    protected Jwt token;

    /**
     * Constructs a {@code JmixJwtAuthenticationToken} using the provided parameters.
     *
     * @param jwt         the JWT
     * @param principal   the principal being authenticated
     * @param authorities the authorities assigned to the JWT
     */
    public JmixJwtAuthenticationToken(Jwt jwt,
                                      JmixOidcUser principal,
                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = jwt;
        this.principal = principal;
        this.setAuthenticated(true);
    }

    /**
     * The principal name
     */
    @Override
    public String getName() {
        return this.principal.getName();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
