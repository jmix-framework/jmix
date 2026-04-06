package io.jmix.saml.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link JmixSamlUserDetails} that delegates some method invocations to the
 * wrapped {@code Saml2AuthenticatedPrincipal}.
 */
public class DefaultJmixSamlUserDetails implements JmixSamlUserDetails, HasSamlPrincipalDelegate {

    private Collection<? extends GrantedAuthority> authorities;

    private Saml2AuthenticatedPrincipal delegate;

    @Override
    public String getName() {
        return delegate.getName();
    }

    public Saml2AuthenticatedPrincipal getDelegate() {
        return delegate;
    }

    public void setDelegate(Saml2AuthenticatedPrincipal delegate) {
        this.delegate = delegate;
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
        return "";
    }

    @Override
    public String getUsername() {
        return getName();
    }

    @Override
    public String getRelyingPartyRegistrationId() {
        return delegate.getRelyingPartyRegistrationId();
    }
}
