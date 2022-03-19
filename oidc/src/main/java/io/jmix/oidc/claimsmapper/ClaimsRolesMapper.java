package io.jmix.oidc.claimsmapper;

import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper of claims received from the OpenID Provider into Jmix resource roles and row-level roles. Some {@link io.jmix.oidc.usermapper.OidcUserMapper}
 * implementations delegate roles mapping to the instance of this interface. If you want to replace standard claims mapper
 * with your own one, then register a spring bean implementing the current interface in your application.
 *
 * @see io.jmix.oidc.usermapper.OidcUserMapper
 */
public interface ClaimsRolesMapper {

    Collection<ResourceRole> toResourceRoles(Map<String, Object> claims);

    Collection<RowLevelRole> toRowLevelRoles(Map<String, Object> claims);

    /**
     * Transforms collection of roles returned by {@link #toResourceRoles(Map)} and {@link #toRowLevelRoles(Map)} into a
     * collection of {@link GrantedAuthority}.
     *
     * @param claims pieces of information about the user returned by the OpenID Provider
     * @return a collection of granted authorities that contain information about resource and row-level roles available
     * to the user
     */
    default Collection<? extends GrantedAuthority> toGrantedAuthorities(Map<String, Object> claims) {
        List<RoleGrantedAuthority> resourceRoleAuthorities = toResourceRoles(claims).stream()
                .map(RoleGrantedAuthority::ofResourceRole)
                .collect(Collectors.toList());
        List<RoleGrantedAuthority> rowLevelRoleAuthorities = toRowLevelRoles(claims).stream()
                .map(RoleGrantedAuthority::ofRowLevelRole)
                .collect(Collectors.toList());
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.addAll(resourceRoleAuthorities);
        grantedAuthorities.addAll(rowLevelRoleAuthorities);
        return grantedAuthorities;
    }
}
