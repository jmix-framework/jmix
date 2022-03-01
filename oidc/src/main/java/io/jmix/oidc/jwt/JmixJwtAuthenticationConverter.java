package io.jmix.oidc.jwt;

import io.jmix.oidc.user.JmixOidcUser;
import io.jmix.oidc.usermapper.OidcUserMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Converts {@link Jwt} into {@link org.springframework.security.core.Authentication}. We cannot use default {@link
 * org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter} shipped with Spring
 * Security because Jmix requires an instance of {@link org.springframework.security.core.userdetails.UserDetails} to be
 * set into the {@code SecurityContext}. That's why the current converter constructs the {@link JmixOidcUser} and sets
 * it as Authentication principal.
 */
public class JmixJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    protected String usernameClaimName = JwtClaimNames.SUB;

    protected OidcUserMapper oidcUserMapper;

    public JmixJwtAuthenticationConverter(OidcUserMapper oidcUserMapper) {
        this.oidcUserMapper = oidcUserMapper;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OidcIdToken oidcIdToken = OidcIdToken.withTokenValue(jwt.getTokenValue())
                .claims(claims -> claims.putAll(jwt.getClaims()))
                .build();
        DefaultOidcUser oidcUser = new DefaultOidcUser(Collections.emptyList(), oidcIdToken, usernameClaimName);
        JmixOidcUser jmixOidcUser = oidcUserMapper.toJmixUser(oidcUser);
        JmixJwtAuthenticationToken token = new JmixJwtAuthenticationToken(jwt,
                jmixOidcUser,
                jmixOidcUser.getAuthorities());
        return token;
    }

    /**
     * Sets the username claim name. Defaults to {@link JwtClaimNames#SUB}.
     *
     * @param usernameClaimName The username claim name
     */
    public void setUsernameClaimName(String usernameClaimName) {
        Assert.hasText(usernameClaimName, "usernameClaimName cannot be empty");
        this.usernameClaimName = usernameClaimName;
    }
}
