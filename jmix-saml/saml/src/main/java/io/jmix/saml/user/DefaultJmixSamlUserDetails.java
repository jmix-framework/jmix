package io.jmix.saml.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultJmixSamlUserDetails implements JmixSamlUserDetails {

    private Collection<? extends GrantedAuthority> authorities;

    private Saml2AuthenticatedPrincipal delegate;

    public DefaultJmixSamlUserDetails(Saml2AuthenticatedPrincipal delegate, Collection<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return getName();
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
        return true;
    }
}
