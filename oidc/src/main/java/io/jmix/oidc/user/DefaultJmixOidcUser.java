package io.jmix.oidc.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/**
 * The implementation of {@link OidcUserDetails} that wraps the {@link OidcUser} provided by the
 * OpenID Connect 1.0 Provider and delegates some method invocations to the wrapped {@code OidcUser}.
 */
public class DefaultJmixOidcUser implements OidcUserDetails, HasOidcUserDelegate {

    private OidcUser delegate;

    private Collection<? extends GrantedAuthority> authorities;

    public DefaultJmixOidcUser(OidcUser delegate, Collection<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.authorities = authorities;
    }

    @Override
    public OidcUser getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(OidcUser delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getPassword() {
        //todo null password
        return null;
    }

    @Override
    public String getUsername() {
        return delegate.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        //todo where to take the enabled info from?
        return true;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
