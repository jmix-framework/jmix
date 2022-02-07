package io.jmix.oidc.usermapper;

import io.jmix.oidc.claimsmapper.OidcClaimsMapper;
import io.jmix.oidc.user.DefaultJmixOidcUser;
import io.jmix.oidc.user.OidcUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

/**
 * The {@link OidcUserMapper} implementation that converts {@link OidcUser} into {@link DefaultJmixOidcUser}
 */
public class OidcSimpleUserMapper implements OidcUserMapper<OidcUserDetails> {

    protected OidcClaimsMapper claimsToGrantedAuthoritiesMapper;

    public OidcSimpleUserMapper(OidcClaimsMapper claimsToGrantedAuthoritiesMapper) {
        this.claimsToGrantedAuthoritiesMapper = claimsToGrantedAuthoritiesMapper;
    }

    @Override
    public DefaultJmixOidcUser toJmixUser(OidcUser oidcUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = claimsToGrantedAuthoritiesMapper.toGrantedAuthorities(oidcUser.getClaims());
        return new DefaultJmixOidcUser(oidcUser, grantedAuthorities);
    }
}
