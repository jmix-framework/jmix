package io.jmix.saml.user;

import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

public interface JmixSamlUserDetails extends JmixUserDetails, Saml2AuthenticatedPrincipal {
}
