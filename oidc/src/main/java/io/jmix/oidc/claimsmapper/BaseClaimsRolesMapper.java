package io.jmix.oidc.claimsmapper;

import io.jmix.oidc.OidcProperties;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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

    //todo setter injection?
    public BaseClaimsRolesMapper(ResourceRoleRepository resourceRoleRepository,
                                 RowLevelRoleRepository rowLevelRoleRepository) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
        this.resourceRoleRepository = resourceRoleRepository;
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
                log.warn("Resource role {} not found", jmixRoleCode);
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
}
