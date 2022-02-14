package io.jmix.oidc.usermapper;

import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.user.DefaultJmixOidcUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

/**
 * The default implementation {@link OidcUserMapper} implementation that converts {@link OidcUser} into {@link
 * DefaultJmixOidcUser}.
 */
public class DefaultOidcUserMapper extends BaseOidcUserMapper<DefaultJmixOidcUser> {

    protected ClaimsRolesMapper claimsRolesMapper;

    public DefaultOidcUserMapper(ClaimsRolesMapper claimsRolesMapper) {
        this.claimsRolesMapper = claimsRolesMapper;
    }

    protected DefaultJmixOidcUser initJmixUser(OidcUser oidcUser) {
        return new DefaultJmixOidcUser();
    }

    @Override
    protected void populateUserAuthorities(OidcUser oidcUser, DefaultJmixOidcUser jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = claimsRolesMapper.toGrantedAuthorities(oidcUser.getClaims());
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void populateUserAttributes(OidcUser oidcUser, DefaultJmixOidcUser jmixUser) {
    }
}
