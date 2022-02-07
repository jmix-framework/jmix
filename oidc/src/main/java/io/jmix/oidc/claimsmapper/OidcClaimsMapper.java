package io.jmix.oidc.claimsmapper;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * Mapper of claims gotten from the OpenID Connect 1.0 provider to Jmix granted authorities (resource roles and
 * row-level roles).
 */
public interface OidcClaimsMapper {

    /**
     * Maps claims values gotten from identity provider to a list of granted authorities acceptable * by Jmix (resource
     * and row-level roles)
     *
     * @param claims
     * @return
     */
    Collection<? extends GrantedAuthority> toGrantedAuthorities(Map<String, Object> claims);
}
