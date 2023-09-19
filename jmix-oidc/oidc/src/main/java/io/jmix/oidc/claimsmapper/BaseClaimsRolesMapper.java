package io.jmix.oidc.claimsmapper;

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.Stream;

/**
 * {@link ClaimsRolesMapper} that can be used as super-class for your own {@link ClaimsRolesMapper} implementations. The
 * child classes must override the {@link #getResourceRolesCodes(Map)} or/and {@link #getRowLevelRoleCodes(Map)}
 * methods. The behavior for finding roles with given codes and transforming them into a collection of {@link
 * org.springframework.security.core.GrantedAuthority} is already implemented in the current class.
 */
public class BaseClaimsRolesMapper implements ClaimsRolesMapper {

    private static final Logger log = LoggerFactory.getLogger(BaseClaimsRolesMapper.class);

    protected RowLevelRoleRepository rowLevelRoleRepository;

    protected ResourceRoleRepository resourceRoleRepository;

    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    //todo setter injection?
    public BaseClaimsRolesMapper(ResourceRoleRepository resourceRoleRepository,
                                 RowLevelRoleRepository rowLevelRoleRepository,
                                 RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
        this.resourceRoleRepository = resourceRoleRepository;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    @Override
    public Collection<ResourceRole> toResourceRoles(Map<String, Object> claims) {
        Collection<ResourceRole> roles = new ArrayList<>();
        Collection<String> jmixRoleCodes = getResourceRolesCodes(claims);
        for (String jmixRoleCode : jmixRoleCodes) {
            ResourceRole jmixRole = resourceRoleRepository.findRoleByCode(jmixRoleCode);
            if (jmixRole != null) {
                roles.add(jmixRole);
            } else {
                log.warn("Resource role {} not found", jmixRoleCode);
            }
        }
        return roles;
    }

    @Override
    public Collection<RowLevelRole> toRowLevelRoles(Map<String, Object> claims) {
        Collection<RowLevelRole> roles = new ArrayList<>();
        Collection<String> jmixRoleCodes = getRowLevelRoleCodes(claims);
        for (String jmixRoleCode : jmixRoleCodes) {
            RowLevelRole jmixRole = rowLevelRoleRepository.findRoleByCode(jmixRoleCode);
            if (jmixRole != null) {
                roles.add(jmixRole);
            } else {
                log.warn("Row-level role {} not found", jmixRoleCode);
            }
        }
        return roles;
    }

    protected Collection<String> getResourceRolesCodes(Map<String, Object> claims) {
        return Collections.emptySet();
    }

    protected Collection<String> getRowLevelRoleCodes(Map<String, Object> claims) {
        return Collections.emptySet();
    }

    /**
     * Transforms collection of roles returned by {@link #toResourceRoles(Map)} and {@link #toRowLevelRoles(Map)} into a
     * collection of {@link GrantedAuthority}.
     *
     * @param claims pieces of information about the user returned by the OpenID Provider
     * @return a collection of granted authorities that contain information about resource and row-level roles available
     * to the user
     */
    @Override
    public Collection<? extends GrantedAuthority> toGrantedAuthorities(Map<String, Object> claims) {
        Stream<GrantedAuthority> resourceRoleAuthoritiesStream = toResourceRoles(claims).stream()
                .map(roleGrantedAuthorityUtils::createResourceRoleGrantedAuthority);
        Stream<GrantedAuthority> rowLevelRoleAuthoritiesStream = toRowLevelRoles(claims).stream()
                .map(roleGrantedAuthorityUtils::createRowLevelRoleGrantedAuthority);
        return Stream.concat(resourceRoleAuthoritiesStream, rowLevelRoleAuthoritiesStream).toList();
    }
}
