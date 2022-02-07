package io.jmix.oidc.claimsmapper;

import io.jmix.oidc.OidcProperties;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Default {@link OidcClaimsMapper} implementation that gets a list of role names from {@code OidcUser} attribute and
 * transforms roles names to resource and row-level roles using role name prefixes.
 * <p>
 * Roles names are taken from a user attribute. Attribute name is taken from the {@link
 * io.jmix.oidc.OidcProperties.ClaimsMapper#getRolesClaimName()}.
 * <p>
 * Role names from the user attribute are mapped to the resource and row-level roles using {@link #resourceRolePrefix}
 * and {@link #rowLevelRolePrefix} prefixes, e.g. if the {@code resourceRolePrefix} is "resource$" then OIDC role with
 * the name "resource$system-full-access" will be mapped to Jmix role with the "system-full-access" code.
 */
public class DefaultOidcClaimsMapper implements OidcClaimsMapper {

    private static final Logger log = LoggerFactory.getLogger(DefaultOidcClaimsMapper.class);

    protected String rolesClaimName;

    protected RowLevelRoleRepository rowLevelRoleRepository;

    protected ResourceRoleRepository resourceRoleRepository;

    protected OidcProperties oidcProperties;

    protected String resourceRolePrefix;

    protected String rowLevelRolePrefix;

    public DefaultOidcClaimsMapper(ResourceRoleRepository resourceRoleRepository,
                                   RowLevelRoleRepository rowLevelRoleRepository,
                                   OidcProperties oidcProperties) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
        this.resourceRoleRepository = resourceRoleRepository;
        this.oidcProperties = oidcProperties;

        this.rolesClaimName = oidcProperties.getClaimsMapper().getRolesClaimName();
        this.resourceRolePrefix = oidcProperties.getClaimsMapper().getResourceRolePrefix();
        this.rowLevelRolePrefix = oidcProperties.getClaimsMapper().getRowLevelRolePrefix();
    }

    @Override
    public Collection<? extends GrantedAuthority> toGrantedAuthorities(Map<String, Object> claims) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object rolesClaimValue = claims.get(rolesClaimName);
        if (rolesClaimValue instanceof Collection) {
            Collection<String> rolesNames = (Collection<String>) rolesClaimValue;
            for (String roleName : rolesNames) {
                Optional<GrantedAuthority> grantedAuthorityOpt = evaluateRoleGrantedAuthority(roleName);
                grantedAuthorityOpt.ifPresent(authorities::add);
            }
        } else {
            log.debug("Roles claim with the name {} doesn't exist", rolesClaimName);
        }
        return authorities;
    }

    protected Optional<GrantedAuthority> evaluateRoleGrantedAuthority(String roleName) {
        if (roleName.startsWith(resourceRolePrefix)) {
            String roleCode = roleName.substring(resourceRolePrefix.length());
            ResourceRole role = resourceRoleRepository.findRoleByCode(roleCode);
            if (role != null) {
                return Optional.of(RoleGrantedAuthority.ofResourceRole(role));
            }
        } else if (roleName.startsWith(rowLevelRolePrefix)) {
            String roleCode = roleName.substring(rowLevelRolePrefix.length());
            RowLevelRole role = rowLevelRoleRepository.findRoleByCode(roleCode);
            if (role != null) {
                return Optional.of(RoleGrantedAuthority.ofRowLevelRole(role));
            }
        }
        return Optional.empty();
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
