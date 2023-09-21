/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.security.impl.role;

import io.jmix.core.CacheOperations;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.role.ResourceRoleProvider;
import io.jmix.security.role.ResourceRoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Component("sec_ResourceRoleRepository")
public class ResourceRoleRepositoryImpl implements ResourceRoleRepository {

    private final CacheManager cacheManager;

    private final CacheOperations cacheOperations;

    private Cache rolesCache;

    private final RoleRepositoryProviderUtils<ResourceRole> roleRepositoryProviderUtils;

    public ResourceRoleRepositoryImpl(CacheManager cacheManager,
                                      CacheOperations cacheOperations,
                                      Collection<ResourceRoleProvider> roleProviders,
                                      ObjectProvider<RoleRepositoryProviderUtils<ResourceRole>> roleRepositoryProviderUtilsProvider) {
        this.cacheManager = cacheManager;
        this.cacheOperations = cacheOperations;
        this.roleRepositoryProviderUtils = roleRepositoryProviderUtilsProvider.getObject(roleProviders);
    }

    @PostConstruct
    public void init() {
        rolesCache = cacheManager.getCache(RESOURCE_ROLES_CACHE_NAME);
        if (rolesCache == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", RESOURCE_ROLES_CACHE_NAME));
        }
    }

    @Override
    public ResourceRole findRoleByCode(String roleCode) {
        return cacheOperations.get(rolesCache, roleCode, () ->
                roleRepositoryProviderUtils.findRoleByCodeExcludeVisited(roleCode,
                        new HashSet<>(),
                        (role, childRole) -> {
                            Collection<ResourcePolicy> allPolicies = new ArrayList<>(role.getAllResourcePolicies());
                            allPolicies.addAll(childRole.getAllResourcePolicies());
                            role.setAllResourcePolicies(allPolicies);
                        }));
    }

    @Override
    public ResourceRole getRoleByCode(String code) {
        ResourceRole rowLevelRole = findRoleByCode(code);
        if (rowLevelRole == null) {
            throw new IllegalStateException(String.format("ResourceRole not found by code: %s", code));
        }
        return rowLevelRole;
    }

    @Override
    public boolean deleteRole(String code) {
        return roleRepositoryProviderUtils.deleteRole(code);
    }

    @Override
    public Collection<ResourceRole> getAllRoles() {
        return roleRepositoryProviderUtils.getAllRoles();
    }

    @Override
    public void invalidateCache() {
        rolesCache.clear();
    }
}
