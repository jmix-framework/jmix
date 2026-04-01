package io.jmix.saml.user;

import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/**
 * Interface to be implemented by a user Principal that is authenticated by SAML. *
 * The interface extends {@link org.springframework.security.core.userdetails.UserDetails} because Jmix framework requires
 * the user principal put to the {@link org.springframework.security.core.context.SecurityContext} to implement it.
 * <p>
 * Implementation of {@link io.jmix.saml.mapper.user.SamlUserMapper} must return instances of this interface.
 */
public interface JmixSamlUserDetails extends JmixUserDetails, Saml2AuthenticatedPrincipal {
}
