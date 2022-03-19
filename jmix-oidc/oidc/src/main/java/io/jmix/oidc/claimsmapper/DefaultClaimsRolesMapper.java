package io.jmix.oidc.claimsmapper;

import io.jmix.oidc.OidcProperties;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default {@link ClaimsRolesMapper} implementation that takes {@link org.springframework.security.oauth2.core.oidc.user.OidcUser}
 * role names and transforms roles names to resource and row-level roles using role name prefixes.
 * <p>
 * Roles names are taken from a special claim. Claim name is taken from the {@link #rolesClaimName} property. The
 * default value is taken from the {@link OidcProperties.DefaultClaimsRolesMapperConfig#getRolesClaimName()} and may be
 * changed using the corresponding application property.
 * <p>
 * Role names from the user attribute are mapped to the resource and row-level roles using {@link #resourceRolePrefix}
 * and {@link #rowLevelRolePrefix} prefixes, e.g. if the {@code resourceRolePrefix} is "resource$" then OIDC role with
 * the name "resource$system-full-access" will be mapped to Jmix role with the "system-full-access" code.
 */
public class DefaultClaimsRolesMapper extends BaseClaimsRolesMapper {

    private static final Logger log = LoggerFactory.getLogger(DefaultClaimsRolesMapper.class);

    protected String rolesClaimName = "roles";

    //todo do we really need this prefixes stuff?
    protected String resourceRolePrefix = "";

    protected String rowLevelRolePrefix = "";

    public DefaultClaimsRolesMapper(ResourceRoleRepository resourceRoleRepository,
                                    RowLevelRoleRepository rowLevelRoleRepository) {
        super(resourceRoleRepository, rowLevelRoleRepository);
    }

    @Override
    protected Collection<String> getResourceRolesCodes(Map<String, Object> claims) {
        return getRolesCodes(claims, rolesClaimName, resourceRolePrefix);
    }

    @Override
    protected Collection<String> getRowLevelRoleCodes(Map<String, Object> claims) {
        return getRolesCodes(claims, rolesClaimName, rowLevelRolePrefix);
    }

    protected Collection<String> getRolesCodes(Map<String, Object> claims, String rolesClaimName, String oidcRoleNamePrefix) {
        Object rolesClaimValue = claims.get(rolesClaimName);
        if (rolesClaimValue instanceof Collection) {
            Collection<String> oidcRolesNames = (Collection<String>) rolesClaimValue;
            return oidcRolesNames.stream()
                    .filter(oidcRoleName -> oidcRoleName.startsWith(oidcRoleNamePrefix))
                    .map(oidcRoleName -> oidcRoleName.substring(oidcRoleNamePrefix.length()))
                    .collect(Collectors.toSet());
        } else {
            log.warn("Roles claim {} doesn't exist", rolesClaimName);
        }
        return Collections.emptySet();
    }

    public String getRolesClaimName() {
        return rolesClaimName;
    }

    public void setRolesClaimName(String rolesClaimName) {
        this.rolesClaimName = rolesClaimName;
    }

    public String getResourceRolePrefix() {
        return resourceRolePrefix;
    }

    public void setResourceRolePrefix(String resourceRolePrefix) {
        this.resourceRolePrefix = resourceRolePrefix;
    }

    public String getRowLevelRolePrefix() {
        return rowLevelRolePrefix;
    }

    public void setRowLevelRolePrefix(String rowLevelRolePrefix) {
        this.rowLevelRolePrefix = rowLevelRolePrefix;
    }

}
