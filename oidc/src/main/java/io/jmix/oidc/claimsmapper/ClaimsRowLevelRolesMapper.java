package io.jmix.oidc.claimsmapper;

import io.jmix.security.model.ResourceRole;

import java.util.Collection;
import java.util.Map;

public interface ClaimsRowLevelRolesMapper {

    Collection<ResourceRole> toResourceRoles(Map<String, Object> claims);
}
